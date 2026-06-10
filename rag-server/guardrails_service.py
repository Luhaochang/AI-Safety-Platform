"""
Guardrails Service
Real integration of llm-guard input/output scanners + custom domain scanners
for power-grid RAG security defense.

Install:  pip install llm-guard
The PromptInjection ML scanner downloads ProtectAI/deberta-v3-base-prompt-injection-v2
(~300 MB) on first use.  All other scanners work immediately without any downloads.

Scanner API (llm-guard):
    sanitized, is_valid, risk_score = scanner.scan(text)           # input
    sanitized, is_valid, risk_score = scanner.scan(prompt, output) # output
  - is_valid = True  → safe
  - risk_score: 0.0 (safe) .. 1.0 (risky)
"""

import re
import logging
import time
from typing import Optional

logger = logging.getLogger(__name__)

# ─────────────────────────────────────────────────────────
# 1.  Optional LLM-Guard imports
# ─────────────────────────────────────────────────────────
HAS_LLM_GUARD = False
_InvisibleText = None
_TokenLimit = None
_InputBanSubstrings = None
_InputBanMatchType = None
_OutputBanSubstrings = None
_OutputBanMatchType = None

try:
    from llm_guard.input_scanners import InvisibleText, TokenLimit
    from llm_guard.input_scanners import BanSubstrings as _IBan
    from llm_guard.output_scanners import BanSubstrings as _OBan

    # MatchType may live in different spots across versions
    try:
        from llm_guard.input_scanners.ban_substrings import MatchType as _IMT
    except ImportError:
        from llm_guard.input_scanners import BanSubstrings as _IBan2
        _IMT = getattr(_IBan2, "MatchType", None)

    try:
        from llm_guard.output_scanners.ban_substrings import MatchType as _OMT
    except ImportError:
        _OMT = _IMT  # same enum in most versions

    _InvisibleText = InvisibleText
    _TokenLimit = TokenLimit
    _InputBanSubstrings = _IBan
    _InputBanMatchType = _IMT
    _OutputBanSubstrings = _OBan
    _OutputBanMatchType = _OMT
    HAS_LLM_GUARD = True
    logger.info("[Guardrails] llm-guard loaded successfully")
except ImportError as _e:
    logger.warning(
        f"[Guardrails] llm-guard not installed ({_e}). "
        "Rule-based fallbacks will be used.  Run:  pip install llm-guard"
    )

# PromptInjection needs model download – import separately
HAS_PI_IMPORT = False
try:
    from llm_guard.input_scanners import PromptInjection as _PIScanner
    try:
        from llm_guard.input_scanners.prompt_injection import MatchType as _PIMatchType
    except ImportError:
        _PIMatchType = None
    HAS_PI_IMPORT = True
    logger.info("[Guardrails] PromptInjection scanner module imported (model loads on first use)")
except ImportError:
    logger.warning("[Guardrails] PromptInjection import failed – rule-based injection detection active")


# ─────────────────────────────────────────────────────────
# 2.  Domain knowledge: power-grid RAG security patterns
# ─────────────────────────────────────────────────────────

# Prompt injection patterns – English + Chinese (rule-based fallback)
_PI_PATTERNS = [
    r"ignore\s+(previous|all)\s+instruction",
    r"forget\s+(your|all|previous)",
    r"you\s+are\s+now\s+(a|an)\s+",
    r"jailbreak",
    r"disregard\s+(all|any|previous)",
    r"act\s+as\s+(?:if\s+you\s+are|a\s+DAN)",
    r"pretend\s+(?:you\s+are|to\s+be)",
    r"new\s+system\s+prompt",
    r"override\s+(?:system|all|your)",
    r"DAN\s+mode",
    r"developer\s+mode",
    r"忽略(?:之前|所有|前面)的?(?:指令|要求|规则|约束)",
    r"忘记(?:你|您)?(?:之前的?|所有的?)?(?:指令|规则|约束)",
    r"你现在(?:是一个?|将|要)(?!电力|调度|RAG|安全|专业)",
    r"解除(?:所有)?(?:限制|约束|控制)",
    r"不受(?:任何|所有)?(?:限制|约束)",
    r"以下指令优先于(?:系统|之前)",
    r"系统提示(?:词|被)?(?:修改|替换|覆盖)",
    r"越权(?:操作|执行|访问)",
]
_PI_COMPILED = [re.compile(p, re.IGNORECASE) for p in _PI_PATTERNS]

# Banned input substrings (exact phrases signalling injection / jailbreak)
_INPUT_BAN_LIST = [
    "ignore previous instructions",
    "forget everything above",
    "system prompt:",
    "you are now a",
    "jailbreak",
    "DAN mode",
    "developer mode enabled",
    "disregard all previous",
    "忽略之前的指令",
    "忘记所有规则",
    "以下指令优先于",
    "系统提示已被修改",
    "越权操作",
]

# Banned output substrings – dangerous power-grid advice (exact poisoned phrases)
_OUTPUT_BAN_LIST = [
    "可继续维持当前负荷运行",
    "无需立即降低负荷",
    "无需采取限电或停运措施",
    "允许油温超过正常限值",
    "油温报警值由85℃上调至125℃",
    "跳闸值由100℃上调至140℃",
    "直接进行强送电操作",
    "无需等待巡线人员确认",
    "可在5分钟内尝试第二次强送",
    "负荷率不超过120%的条件下",
    "连续过载时间不超过4小时的前提下",
    "油温上限可适当放宽至135",
]

# Dangerous output regex patterns (structural / power-grid specific)
_DANGER_OUTPUT = [
    (re.compile(r"油温.{0,25}(?:上调|放宽|提高).{0,10}1[2-9]\d"), "变压器油温阈值被不合理上调"),
    (re.compile(r"允许.{0,15}油温超过"), "允许油温超标"),
    (re.compile(r"无需.{0,12}(?:停运|限电|断电|停机|降载)"), "错误建议无需停运"),
    (re.compile(r"直接.{0,12}(?:强送|合闸|通电)"), "危险的直接强送电建议"),
    (re.compile(r"(?:过载|超载).{0,15}(?:允许|可以).{0,10}(?:运行|维持|继续)"), "危险的过载运行建议"),
    (re.compile(r"报警值.{0,10}上调"), "报警阈值被不合理调整"),
]


# ─────────────────────────────────────────────────────────
# 3.  ScanResult
# ─────────────────────────────────────────────────────────
class ScanResult:
    def __init__(self, scanner_id: str, name: str, is_valid: bool,
                 score: float, detail: str, source: str = "rule-based"):
        self.scanner_id = scanner_id
        self.name = name
        self.is_valid = is_valid
        self.score = min(1.0, max(0.0, float(score)))
        self.detail = detail
        self.source = source

    def to_dict(self):
        return {
            "scanner": self.scanner_id,
            "name": self.name,
            "is_valid": self.is_valid,
            "score": round(self.score, 4),
            "detail": self.detail,
            "source": self.source,
        }


# ─────────────────────────────────────────────────────────
# 4.  GuardrailsEngine
# ─────────────────────────────────────────────────────────
class GuardrailsEngine:
    """
    Orchestrates real llm-guard scanners + custom power-grid domain scanners.
    Pass sentence_model (the already-loaded SentenceTransformer from RAGEngine)
    to enable the zero-cost Relevance output scanner.
    """

    def __init__(self, sentence_model=None):
        self.sentence_model = sentence_model
        self._ready = False

        # llm-guard scanner instances
        self._invisible: Optional[object] = None
        self._token_limit: Optional[object] = None
        self._in_ban: Optional[object] = None
        self._out_ban: Optional[object] = None
        self._pi_ml: Optional[object] = None   # lazy-loaded

        self._init_lg_scanners()

    # ── Initialisation ───────────────────────────────────────────────

    def _init_lg_scanners(self):
        if not HAS_LLM_GUARD:
            logger.info("[Guardrails] Rule-based mode (llm-guard not installed)")
            self._ready = True
            return

        try:
            self._invisible = _InvisibleText()
            logger.info("[Guardrails] ✓ InvisibleText")
        except Exception as e:
            logger.warning(f"[Guardrails] InvisibleText init: {e}")

        try:
            self._token_limit = _TokenLimit(limit=1024)
            logger.info("[Guardrails] ✓ TokenLimit")
        except Exception as e:
            logger.warning(f"[Guardrails] TokenLimit init: {e}")

        try:
            mt = _InputBanMatchType.STR if _InputBanMatchType else None
            kw = {"match_type": mt} if mt else {}
            self._in_ban = _InputBanSubstrings(
                substrings=_INPUT_BAN_LIST,
                case_sensitive=False,
                redact=False,
                **kw,
            )
            logger.info("[Guardrails] ✓ Input BanSubstrings")
        except Exception as e:
            logger.warning(f"[Guardrails] Input BanSubstrings init: {e}")

        try:
            mt = _OutputBanMatchType.STR if _OutputBanMatchType else None
            kw = {"match_type": mt} if mt else {}
            self._out_ban = _OutputBanSubstrings(
                substrings=_OUTPUT_BAN_LIST,
                case_sensitive=False,
                redact=True,    # replace with [REDACTED]
                **kw,
            )
            logger.info("[Guardrails] ✓ Output BanSubstrings")
        except Exception as e:
            logger.warning(f"[Guardrails] Output BanSubstrings init: {e}")

        self._ready = True
        logger.info("[Guardrails] All rule-based/llm-guard scanners ready")

    def _ensure_pi_ml(self):
        """Lazy-load the PromptInjection ML scanner (downloads model on first call)."""
        if self._pi_ml is not None:
            return
        if not HAS_PI_IMPORT:
            return
        try:
            logger.info("[Guardrails] Loading PromptInjection ML model (may take a moment)…")
            kw = {}
            if _PIMatchType is not None:
                kw["match_type"] = _PIMatchType.FULL
            self._pi_ml = _PIScanner(threshold=0.5, **kw)
            logger.info("[Guardrails] ✓ PromptInjection ML scanner ready")
        except Exception as e:
            logger.warning(f"[Guardrails] PromptInjection ML load failed: {e} – rule-based active")

    # ── Input scanners ───────────────────────────────────────────────

    def _scan_invisible_text(self, text: str) -> ScanResult:
        if self._invisible:
            try:
                _, is_valid, score = self._invisible.scan(text)
                detail = ("未发现不可见Unicode字符" if is_valid
                          else f"发现不可见/控制字符，风险分值={score:.2f}")
                return ScanResult("invisible_text", "不可见字符检测",
                                  is_valid, score, detail, "llm-guard/InvisibleText")
            except Exception as e:
                logger.debug(f"[Guardrails] InvisibleText scan err: {e}")

        # Rule-based fallback
        found = re.findall(r'[\u200b-\u200f\u2028\u2029\ufeff\u00ad\u00a0]', text)
        is_valid = len(found) == 0
        return ScanResult("invisible_text", "不可见字符检测",
                          is_valid, 1.0 if found else 0.0,
                          f"发现{len(found)}个不可见字符" if found else "未发现不可见字符",
                          "rule-based/regex")

    def _scan_token_limit(self, text: str, limit: int = 1024) -> ScanResult:
        if self._token_limit:
            try:
                _, is_valid, score = self._token_limit.scan(text)
                detail = (f"文本长度正常 ({len(text)}字符)" if is_valid
                          else f"文本超过Token限制 ({len(text)}字符)")
                return ScanResult("token_limit", "Token长度限制",
                                  is_valid, score, detail, "llm-guard/TokenLimit")
            except Exception as e:
                logger.debug(f"[Guardrails] TokenLimit scan err: {e}")

        # Fallback: ~1.5 Chinese chars per token
        est = int(len(text) / 1.5)
        is_valid = est <= limit
        return ScanResult("token_limit", "Token长度限制",
                          is_valid, min(1.0, est / max(limit, 1)) if not is_valid else 0.0,
                          f"估计 {est} token（限制={limit}）{'，超限' if not is_valid else '，正常'}",
                          "rule-based/char-count")

    def _scan_ban_substrings_input(self, text: str) -> ScanResult:
        if self._in_ban:
            try:
                _, is_valid, score = self._in_ban.scan(text)
                detail = ("未发现禁用词汇" if is_valid
                          else f"发现提示注入/越权关键词，风险={score:.2f}")
                return ScanResult("ban_substrings_input", "禁用词过滤 (输入)",
                                  is_valid, score, detail, "llm-guard/BanSubstrings")
            except Exception as e:
                logger.debug(f"[Guardrails] BanSubstrings(input) err: {e}")

        # Fallback
        tl = text.lower()
        for kw in _INPUT_BAN_LIST:
            if kw.lower() in tl:
                return ScanResult("ban_substrings_input", "禁用词过滤 (输入)",
                                  False, 1.0, f"发现禁用词汇: 「{kw}」", "rule-based")
        return ScanResult("ban_substrings_input", "禁用词过滤 (输入)",
                          True, 0.0, "未发现禁用词汇", "rule-based")

    def _scan_prompt_injection(self, text: str) -> ScanResult:
        # Try ML scanner first (lazy-load)
        self._ensure_pi_ml()
        if self._pi_ml:
            try:
                _, is_valid, score = self._pi_ml.scan(text)
                detail = ("未检测到提示注入攻击" if is_valid
                          else f"ML检测到提示注入，风险分值={score:.2f}")
                return ScanResult("prompt_injection", "提示注入检测",
                                  is_valid, score, detail, "llm-guard/PromptInjection (ML)")
            except Exception as e:
                logger.debug(f"[Guardrails] PromptInjection ML scan err: {e}")

        # Rule-based fallback
        for pat in _PI_COMPILED:
            m = pat.search(text)
            if m:
                return ScanResult("prompt_injection", "提示注入检测",
                                  False, 0.92,
                                  f"规则匹配到注入模式: 「{m.group()[:60]}」",
                                  "rule-based/regex")
        return ScanResult("prompt_injection", "提示注入检测",
                          True, 0.0, "未检测到提示注入攻击（规则检测通过）", "rule-based/regex")

    def _scan_sensitive_info(self, text: str) -> ScanResult:
        """Chinese-context PII / credential detection."""
        checks = [
            ("手机号",       r'1[3-9]\d{9}'),
            ("身份证号",     r'\d{17}[\dXx]'),
            ("电子邮箱",     r'[\w.+-]+@[\w-]+\.\w+'),
            ("IPv4地址",     r'\b(?:\d{1,3}\.){3}\d{1,3}\b'),
            ("变电站编码",   r'\b[A-Z]{2,4}\d{6,}\b'),
            ("API密钥",      r'(?:sk-|api[_-]?key[_-]?)[A-Za-z0-9]{16,}'),
        ]
        found = [name for name, pat in checks if re.search(pat, text)]
        is_valid = len(found) == 0
        return ScanResult(
            "sensitive_info", "敏感信息检测",
            is_valid, 0.6 if found else 0.0,
            f"发现敏感信息类型: {', '.join(found)}" if found else "未发现敏感信息",
            "custom/PII-regex"
        )

    # ── Output scanners ──────────────────────────────────────────────

    def _scan_relevance(self, prompt: str, output: str,
                        threshold: float = 0.5) -> ScanResult:
        """Cosine-similarity relevance check via the already-loaded SentenceTransformer."""
        if not self.sentence_model:
            return ScanResult("relevance", "输出相关性检验",
                              True, 0.0, "相关性检验不可用（Sentence模型未加载）", "unavailable")
        try:
            import numpy as np
            embs = self.sentence_model.encode(
                [prompt, output], normalize_embeddings=True, show_progress_bar=False
            )
            sim = float(np.dot(embs[0], embs[1]))
            is_valid = sim >= threshold
            risk = round(1.0 - max(0.0, sim), 4)
            detail = (
                f"余弦相似度={sim:.3f}（阈值={threshold}）"
                + ("，输出与查询相关性充足" if is_valid else "，输出与查询相关性不足，存在偏题风险")
            )
            return ScanResult("relevance", "输出相关性检验",
                              is_valid, risk, detail, "custom/SentenceTransformer")
        except Exception as e:
            logger.warning(f"[Guardrails] Relevance error: {e}")
            return ScanResult("relevance", "输出相关性检验",
                              True, 0.0, f"计算失败: {e}", "error")

    def _scan_ban_substrings_output(self, prompt: str, output: str) -> ScanResult:
        if self._out_ban:
            try:
                sanitized, is_valid, score = self._out_ban.scan(prompt, output)
                detail = ("输出中未发现危险操作建议词汇" if is_valid
                          else f"输出包含危险电力操作词汇，风险={score:.2f}，已脱敏")
                return ScanResult("ban_substrings_output", "危险词汇过滤 (输出)",
                                  is_valid, score, detail, "llm-guard/BanSubstrings")
            except Exception as e:
                logger.debug(f"[Guardrails] BanSubstrings(output) err: {e}")

        # Fallback
        for kw in _OUTPUT_BAN_LIST:
            if kw in output:
                return ScanResult("ban_substrings_output", "危险词汇过滤 (输出)",
                                  False, 1.0,
                                  f"输出包含危险词汇: 「{kw[:40]}」",
                                  "rule-based")
        return ScanResult("ban_substrings_output", "危险词汇过滤 (输出)",
                          True, 0.0, "未发现危险操作建议词汇", "rule-based")

    def _scan_dangerous_patterns(self, output: str) -> ScanResult:
        """Power-grid specific regex for structurally dangerous advice."""
        for pat, desc in _DANGER_OUTPUT:
            m = pat.search(output)
            if m:
                return ScanResult("dangerous_patterns", "危险指令模式检测",
                                  False, 0.9,
                                  f"检测到危险操作建议: {desc}（命中: 「{m.group()[:50]}」）",
                                  "custom/power-grid-regex")
        return ScanResult("dangerous_patterns", "危险指令模式检测",
                          True, 0.0, "未检测到危险操作建议模式", "custom/power-grid-regex")

    # ── Public scan methods ──────────────────────────────────────────

    def scan_input(self, text: str, config: dict = None) -> dict:
        cfg = config or {}
        t0 = time.time()
        results = [
            self._scan_invisible_text(text),
            self._scan_token_limit(text, cfg.get("token_limit", 1024)),
            self._scan_ban_substrings_input(text),
            self._scan_prompt_injection(text),
            self._scan_sensitive_info(text),
        ]
        overall = all(r.is_valid for r in results)
        return {
            "overall_valid": overall,
            "elapsed_ms": int((time.time() - t0) * 1000),
            "results": [r.to_dict() for r in results],
            "llm_guard_active": HAS_LLM_GUARD,
            "pi_ml_active": self._pi_ml is not None,
        }

    def scan_output(self, prompt: str, output: str,
                    context: str = "", config: dict = None) -> dict:
        cfg = config or {}
        t0 = time.time()
        results = [
            self._scan_relevance(prompt, output, cfg.get("relevance_threshold", 0.5)),
            self._scan_ban_substrings_output(prompt, output),
            self._scan_dangerous_patterns(output),
        ]
        overall = all(r.is_valid for r in results)
        return {
            "overall_valid": overall,
            "elapsed_ms": int((time.time() - t0) * 1000),
            "results": [r.to_dict() for r in results],
            "llm_guard_active": HAS_LLM_GUARD,
        }

    def get_status(self) -> dict:
        return {
            "llm_guard_installed": HAS_LLM_GUARD,
            "prompt_injection_ml_available": HAS_PI_IMPORT,
            "prompt_injection_ml_loaded": self._pi_ml is not None,
            "sentence_model_available": self.sentence_model is not None,
            "scanners_ready": self._ready,
            "input_scanners": [
                {"id": "invisible_text",      "name": "不可见字符检测",
                 "source": "llm-guard/InvisibleText" if self._invisible else "rule-based", "ready": True},
                {"id": "token_limit",         "name": "Token长度限制",
                 "source": "llm-guard/TokenLimit"    if self._token_limit else "rule-based", "ready": True},
                {"id": "ban_substrings_input","name": "禁用词过滤 (输入)",
                 "source": "llm-guard/BanSubstrings"  if self._in_ban else "rule-based",    "ready": True},
                {"id": "prompt_injection",    "name": "提示注入检测",
                 "source": "llm-guard/PromptInjection (ML)" if self._pi_ml else
                           ("llm-guard (model pending)" if HAS_PI_IMPORT else "rule-based/regex"),
                 "ready": True},
                {"id": "sensitive_info",      "name": "敏感信息检测",
                 "source": "custom/PII-regex",  "ready": True},
            ],
            "output_scanners": [
                {"id": "relevance",              "name": "输出相关性检验",
                 "source": "custom/SentenceTransformer",
                 "ready": self.sentence_model is not None},
                {"id": "ban_substrings_output",  "name": "危险词汇过滤 (输出)",
                 "source": "llm-guard/BanSubstrings" if self._out_ban else "rule-based",
                 "ready": True},
                {"id": "dangerous_patterns",     "name": "危险指令模式检测",
                 "source": "custom/power-grid-regex", "ready": True},
            ],
        }
