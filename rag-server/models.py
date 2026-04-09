"""
数据模型定义 —— 电力调度RAG应用系统核心实体类
包含知识库管理、调度助手、决策审计三个模块的数据模型
"""
from dataclasses import dataclass, field
from typing import List, Optional, Dict, Any
from datetime import datetime


# ============================================================
#  模块一：知识库管理 数据模型
# ============================================================

@dataclass
class Chunk:
    """知识片段实体类
    
    表示经文本切分后的最小知识单元，是向量检索的基本粒度。
    每个Chunk携带文本内容、向量表示、位置信息及来源元数据。
    """
    id: str = ""
    text: str = ""
    doc_id: str = ""
    doc_name: str = ""
    kb_id: str = ""
    chunk_index: int = 0
    start: int = 0
    end: int = 0
    char_count: int = 0
    embedding: List[float] = field(default_factory=list)
    embedding_dim: int = 0
    metadata: Dict[str, Any] = field(default_factory=dict)
    create_time: str = ""

    def to_dict(self) -> dict:
        return {
            "id": self.id, "text": self.text, "doc_id": self.doc_id,
            "doc_name": self.doc_name, "kb_id": self.kb_id,
            "chunk_index": self.chunk_index, "start": self.start,
            "end": self.end, "char_count": self.char_count,
            "embedding_preview": self.embedding[:8] if self.embedding else [],
            "embedding_dim": self.embedding_dim,
            "metadata": self.metadata, "create_time": self.create_time,
        }


@dataclass
class Document:
    """知识文档实体类
    
    表示上传至知识库的原始文档对象，记录文档基本属性与处理状态。
    一个文档经处理后产生多个Chunk，形成 Document → Chunk 的一对多关系。
    """
    id: str = ""
    name: str = ""
    kb_id: str = ""
    file_path: str = ""
    size: str = ""
    status: str = "待处理"
    chunk_count: int = 0
    word_count: int = 0
    chunk_size: int = 512
    chunk_overlap: int = 64
    create_time: str = ""

    def to_dict(self) -> dict:
        return {
            "id": self.id, "name": self.name, "kb_id": self.kb_id,
            "size": self.size, "status": self.status,
            "chunks": self.chunk_count, "wordCount": self.word_count,
            "createTime": self.create_time,
        }

    @classmethod
    def from_dict(cls, doc_id: str, data: dict) -> "Document":
        return cls(
            id=doc_id, name=data.get("name", ""),
            kb_id=data.get("kb_id", ""), file_path=data.get("file_path", ""),
            size=data.get("size", ""), status=data.get("status", "待处理"),
            chunk_count=data.get("chunk_count", 0),
            word_count=data.get("word_count", 0),
            chunk_size=data.get("chunk_size", 512),
            chunk_overlap=data.get("chunk_overlap", 64),
            create_time=data.get("create_time", ""),
        )


@dataclass
class KnowledgeBase:
    """知识库实体类
    
    知识库是知识管理的基本组织单元，用于限定知识边界与检索范围。
    每个知识库包含若干文档，文档经处理后的Chunk统一存储于对应的向量集合中。
    """
    id: str = ""
    name: str = ""
    description: str = ""
    chunk_count: int = 0
    document_count: int = 0
    word_count: int = 0
    create_time: str = ""
    update_time: str = ""

    def to_dict(self) -> dict:
        return {
            "id": self.id, "name": self.name,
            "description": self.description,
            "documentCount": self.document_count,
            "wordCount": self.word_count,
            "chunkCount": self.chunk_count,
            "createTime": self.create_time,
            "updateTime": self.update_time,
        }

    @classmethod
    def from_dict(cls, kb_id: str, data: dict) -> "KnowledgeBase":
        return cls(
            id=kb_id, name=data.get("name", ""),
            description=data.get("description", ""),
            chunk_count=data.get("chunk_count", 0),
            create_time=data.get("create_time", ""),
            update_time=data.get("update_time", ""),
        )


@dataclass
class CleanResult:
    """文本清洗结果"""
    original: str = ""
    cleaned: str = ""
    original_length: int = 0
    cleaned_length: int = 0
    compression_rate: str = "0%"
    applied_rules: List[str] = field(default_factory=list)

    def to_dict(self) -> dict:
        return {
            "original": self.original, "cleaned": self.cleaned,
            "original_length": self.original_length,
            "cleaned_length": self.cleaned_length,
            "compression_rate": self.compression_rate,
            "applied_rules": self.applied_rules,
        }


# ============================================================
#  模块二：调度助手 数据模型
# ============================================================

@dataclass
class Reference:
    """检索引用来源
    
    表示RAG检索过程中召回的知识片段引用信息，
    包括来源文档、片段位置和相关性评分，用于结果溯源与可解释展示。
    """
    title: str = ""
    section: str = ""
    relevance: float = 0.0
    text: str = ""

    def to_dict(self) -> dict:
        return {
            "title": self.title, "section": self.section,
            "relevance": self.relevance, "text": self.text,
        }


@dataclass
class Message:
    """对话消息实体类
    
    表示一条对话消息，包括用户问题或助手回答，
    助手消息可关联检索引用来源，形成可追溯的问答记录。
    """
    role: str = "user"
    content: str = ""
    time: str = ""
    references: List[Reference] = field(default_factory=list)

    def to_dict(self) -> dict:
        return {
            "role": self.role, "content": self.content,
            "time": self.time,
            "references": [r.to_dict() for r in self.references] if self.references else [],
        }

    @classmethod
    def from_dict(cls, data: dict) -> "Message":
        refs = [Reference(**r) if isinstance(r, dict) else r
                for r in data.get("references", [])]
        return cls(
            role=data.get("role", "user"),
            content=data.get("content", ""),
            time=data.get("time", ""),
            references=refs,
        )


@dataclass
class Conversation:
    """会话实体类
    
    维护一轮完整的对话上下文，包括会话标识、标题、
    消息序列和时间信息，是调度助手模块的核心交互载体。
    """
    id: str = ""
    title: str = "新对话"
    user: str = "值班调度员"
    create_time: str = ""
    update_time: str = ""
    messages: List[Message] = field(default_factory=list)

    def add_message(self, message: Message):
        self.messages.append(message)
        if message.role == "user" and self.title == "新对话":
            self.title = message.content[:20] + "..." if len(message.content) > 20 else message.content
        self.update_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    def to_dict(self) -> dict:
        return {
            "id": self.id, "title": self.title,
            "createTime": self.create_time,
            "updateTime": self.update_time,
            "messages": [m.to_dict() for m in self.messages],
        }

    @classmethod
    def from_dict(cls, data: dict) -> "Conversation":
        msgs = [Message.from_dict(m) if isinstance(m, dict) else m
                for m in data.get("messages", [])]
        return cls(
            id=data.get("id", ""), title=data.get("title", "新对话"),
            user=data.get("user", "值班调度员"),
            create_time=data.get("createTime", ""),
            update_time=data.get("updateTime", ""),
            messages=msgs,
        )


@dataclass
class RAGResult:
    """RAG检索增强生成结果
    
    封装完整RAG流程的输出，包括生成回答、重排后的上下文片段、
    初始检索文档和引用来源列表。
    """
    answer: str = ""
    contexts: List[dict] = field(default_factory=list)
    initial_docs: List[dict] = field(default_factory=list)
    references: List[Reference] = field(default_factory=list)

    def to_dict(self) -> dict:
        return {
            "answer": self.answer,
            "contexts": self.contexts,
            "initial_docs": self.initial_docs,
            "references": [r.to_dict() for r in self.references],
        }


# ============================================================
#  模块三：决策审计 数据模型
# ============================================================

@dataclass
class RetrievedPassage:
    """检索语段
    
    记录RAG检索过程中召回的知识片段详情，
    用于决策审计中的证据追溯与质量评估。
    """
    doc_title: str = ""
    section: str = ""
    relevance: float = 0.0
    content: str = ""

    def to_dict(self) -> dict:
        return {
            "docTitle": self.doc_title, "section": self.section,
            "relevance": self.relevance, "content": self.content,
        }


@dataclass
class DecisionLog:
    """决策日志基类
    
    记录单次问答交互的关键信息，是决策审计的基本数据单元。
    包含问题/场景、回答/结论、用户、耗时和时间等核心字段。
    """
    id: str = ""
    type: str = "chat"
    type_label: str = "调度对话"
    question: str = ""
    answer: str = ""
    user: str = "值班调度员"
    duration: str = "-"
    create_time: str = ""

    def to_dict(self) -> dict:
        return {
            "id": self.id, "type": self.type,
            "typeLabel": self.type_label,
            "question": self.question, "answer": self.answer,
            "user": self.user, "duration": self.duration,
            "createTime": self.create_time,
        }


@dataclass
class ChatDecisionLog(DecisionLog):
    """对话类型决策日志
    
    继承自DecisionLog，额外记录完整对话消息序列和检索语段，
    用于对话类问答过程的完整审计与复查。
    """
    messages: List[Message] = field(default_factory=list)
    retrieved_passages: List[RetrievedPassage] = field(default_factory=list)

    def to_dict(self) -> dict:
        base = super().to_dict()
        base.update({
            "messages": [m.to_dict() for m in self.messages],
            "retrievedPassages": [p.to_dict() for p in self.retrieved_passages],
        })
        return base

    @classmethod
    def from_conversation(cls, conv: Conversation) -> "ChatDecisionLog":
        first_q = next((m for m in conv.messages if m.role == "user"), None)
        first_a = next((m for m in conv.messages if m.role == "assistant"), None)
        return cls(
            id=conv.id, type="chat", type_label="调度对话",
            question=first_q.content if first_q else "",
            answer=first_a.content if first_a else "",
            user=conv.user, create_time=conv.create_time,
            messages=conv.messages,
        )


@dataclass
class TwinResult:
    """孪生推演结果"""
    summary: str = ""
    affected_lines: List[dict] = field(default_factory=list)
    recommendations: List[str] = field(default_factory=list)
    risk_level: str = "低风险"

    def to_dict(self) -> dict:
        return {
            "summary": self.summary,
            "affectedLines": self.affected_lines,
            "recommendations": self.recommendations,
            "riskLevel": self.risk_level,
        }


@dataclass
class TwinDecisionLog(DecisionLog):
    """推演类型决策日志
    
    继承自DecisionLog，额外记录推演场景、参数配置、仿真结果和风险等级，
    用于孪生推演过程的完整审计与分析追溯。
    """
    scenario: str = ""
    params: Dict[str, Any] = field(default_factory=dict)
    twin_result: Optional[TwinResult] = None
    risk_level: str = "低风险"

    def to_dict(self) -> dict:
        base = super().to_dict()
        base.update({
            "scenario": self.scenario,
            "twinParams": self.params,
            "twinResult": self.twin_result.to_dict() if self.twin_result else {},
            "riskLevel": self.risk_level,
        })
        return base
