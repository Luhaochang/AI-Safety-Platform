import {defineConfig, loadEnv} from 'vite'
import path from 'path'
import createVitePlugins from './vite/plugins'
import tailwindcss from 'tailwindcss'
import autoprefixer from 'autoprefixer'
import { spawn } from 'child_process'

// Vite 插件：dev server 启动时自动拉起 rag-server 后端
function ragServerPlugin() {
    let ragProcess = null;
    return {
        name: 'auto-start-rag-server',
        configureServer() {
            const ragDir = path.resolve(__dirname, '../rag-server');
            console.log('\x1b[36m[RAG] 正在自动启动 rag-server ...\x1b[0m');
            const pythonPath = 'D:\\anaconda3\\envs\\boga\\python.exe';
            ragProcess = spawn(pythonPath, ['main.py'], {
                cwd: ragDir,
                stdio: ['ignore', 'pipe', 'pipe'],
                shell: true
            });
            ragProcess.stdout.on('data', (data) => {
                process.stdout.write(`\x1b[36m[RAG] ${data}\x1b[0m`);
            });
            ragProcess.stderr.on('data', (data) => {
                process.stderr.write(`\x1b[33m[RAG-ERR] ${data}\x1b[0m`);
            });
            ragProcess.on('close', (code) => {
                if (code !== 0) console.log(`\x1b[31m[RAG] rag-server 退出，代码: ${code}\x1b[0m`);
            });
            // 前端 dev server 关闭时也关闭 rag-server
            process.on('exit', () => { if (ragProcess) ragProcess.kill(); });
            process.on('SIGINT', () => { if (ragProcess) ragProcess.kill(); process.exit(); });
            process.on('SIGTERM', () => { if (ragProcess) ragProcess.kill(); process.exit(); });
        }
    };
}

// https://vitejs.dev/config/
export default defineConfig(({mode, command}) => {
    const env = loadEnv(mode, process.cwd())
    const {VITE_APP_ENV} = env
    return {
        // 部署生产环境和开发环境下的URL。
        // 默认情况下，vite 会假设你的应用是被部署在一个域名的根路径上
        base: VITE_APP_ENV === 'production' ? '/' : '/',
        plugins: [
            createVitePlugins(env, command === 'build'),
            // command === 'serve' ? ragServerPlugin() : null,  // 已禁用自动启动后端
        ].filter(Boolean),
        resolve: {
            // https://cn.vitejs.dev/config/#resolve-alias
            alias: {
                // 设置路径
                '~': path.resolve(__dirname, './'),
                // 设置别名
                '@': path.resolve(__dirname, './src')
            },
            // https://cn.vitejs.dev/config/#resolve-extensions
            extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.vue']
        },
        // vite 相关配置
        server: {
            port: 5173,
            host: true,
            open: false,
            proxy: {
                // https://cn.vitejs.dev/config/#server-proxy
                '/api': {
                    target: 'http://localhost:8080',
                    changeOrigin: true,
                    rewrite: (p) => p.replace(/^\/api/, '/')
                },
                '/dashscope-api': {
                    target: 'https://dashscope.aliyuncs.com',
                    changeOrigin: true,
                    rewrite: (p) => p.replace(/^\/dashscope-api/, '')
                },
                '/rag-api': {
                    target: 'http://localhost:8765',
                    changeOrigin: true,
                    rewrite: (p) => p.replace(/^\/rag-api/, '/api/rag')
                }
            }
        },
        //fix:error:stdin>:7356:1: warning: "@charset" must be the first rule in the file
        css: {
            postcss: {
                plugins: [
                    {
                        postcssPlugin: 'internal:charset-removal',
                        AtRule: {
                            charset: (atRule) => {
                                if (atRule.name === 'charset') {
                                    atRule.remove();
                                }
                            }
                        }
                    },
                    tailwindcss, autoprefixer()
                ]
            }
        }
    }
})
