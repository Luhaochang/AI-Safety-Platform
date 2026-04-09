import {createWebHistory, createRouter, createWebHashHistory} from 'vue-router'
/* Layout */
import Layout from '@/layout'

/**
 * Note: 路由配置项
 *
 * hidden: true                     // 当设置 true 的时候该路由不会再侧边栏出现 如401，login等页面，或者如一些编辑页面/edit/1
 * alwaysShow: true                 // 当你一个路由下面的 children 声明的路由大于1个时，自动会变成嵌套的模式--如组件页面
 *                                  // 只有一个时，会将那个子路由当做根路由显示在侧边栏--如引导页面
 *                                  // 若你想不管路由下面的 children 声明的个数都显示你的根路由
 *                                  // 你可以设置 alwaysShow: true，这样它就会忽略之前定义的规则，一直显示根路由
 * redirect: noRedirect             // 当设置 noRedirect 的时候该路由在面包屑导航中不可被点击
 * name:'router-name'               // 设定路由的名字，一定要填写不然使用<keep-alive>时会出现各种问题
 * query: '{"id": 1, "name": "ry"}' // 访问路由的默认传递参数
 * roles: ['admin', 'common']       // 访问路由的角色权限
 * permissions: ['a:a:a', 'b:b:b']  // 访问路由的菜单权限
 * meta : {
    noCache: true                   // 如果设置为true，则不会被 <keep-alive> 缓存(默认 false)
    title: 'title'                  // 设置该路由在侧边栏和面包屑中展示的名字
    icon: 'svg-name'                // 设置该路由的图标，对应路径src/assets/icons/svg
    breadcrumb: false               // 如果设置为false，则不会在breadcrumb面包屑中显示
    activeMenu: '/system/user'      // 当路由设置了该属性，则会高亮相对应的侧边栏。
  }
 */

// 公共路由
export const constantRoutes = [
  {
    path: '/redirect',
    component: Layout,
    hidden: true,
    children: [
      {
        path: '/redirect/:path(.*)',
        component: () => import('@/views/redirect/index.vue')
      }
    ]
  },
  {
    path: '/login',
    component: () => import('@/views/login'),
    hidden: true
  },
  {
    path: '/register',
    component: () => import('@/views/register'),
    hidden: true
  },
  {
    path: "/:pathMatch(.*)*",
    component: () => import('@/views/error/404'),
    hidden: true
  },
  {
    path: '/401',
    component: () => import('@/views/error/401'),
    hidden: true
  },
  {
    path: '',
    component: Layout,
    redirect: '/index',
    children: [
      {
        path: '/index',
        component: () => import('@/views/index'),
        name: 'Index',
        meta: { title: '首页', icon: 'dashboard', affix: true }
      }
    ]
  },
  {
    path: '/user',
    component: Layout,
    hidden: true,
    redirect: 'noredirect',
    children: [
      {
        path: 'profile',
        component: () => import('@/views/system/user/profile/index'),
        name: 'Profile',
        meta: { title: '个人中心', icon: 'user' }
      }
    ]
  },
  // ====== 数据资产管理 ======
  {
    path: '/datamag',
    component: Layout,
    redirect: '/datamag/data-collect',
    name: 'DataMag',
    alwaysShow: true,
    meta: { title: '数据资产管理', icon: 'redis' },
    children: [
      {
        path: 'data-collect',
        component: () => import('@/views/dataGovernance/dataCollect.vue'),
        name: 'DataCollect',
        meta: { title: '采集清洗', icon: 'download' }
      },
      {
        path: 'dataset',
        component: () => import('@/views/dataset/index.vue'),
        name: 'Dataset',
        meta: { title: '数据集创建', icon: 'list' }
      },
      {
        path: 'mark-task',
        component: () => import('@/views/dataset/dataMark/index.vue'),
        name: 'MarkTask',
        meta: { title: '数据标注', icon: 'code' }
      },
      {
        path: 'asset-manage',
        component: () => import('@/views/dataGovernance/assetDetails.vue'),
        name: 'AssetManage',
        meta: { title: '资产管理', icon: 'table' }
      }
    ]
  },
  // ====== 模型训练开发 ======
  {
    path: '/modelmag',
    component: Layout,
    redirect: '/modelmag/model-dev',
    name: 'ModelMag',
    alwaysShow: true,
    meta: { title: '模型训练开发', icon: 'component' },
    children: [
      {
        path: 'model-dev',
        component: () => import('@/views/model/line/index.vue'),
        name: 'ModelDev',
        meta: { title: '模型开发', icon: 'tree' }
      },
      {
        path: 'model',
        component: () => import('@/views/model/modelMag/index.vue'),
        name: 'ModelSpace',
        meta: { title: '模型空间', icon: 'build' }
      },
      {
        path: 'service-list',
        component: () => import('@/views/model/service/modelList.vue'),
        name: 'ServiceList',
        hidden: true,
        meta: { title: '部署服务', icon: 'job', activeMenu: '/modelmag/model' }
      },
      {
        path: 'train-vis',
        component: () => import('@/views/model/trainVis/index.vue'),
        name: 'TrainVis',
        meta: { title: '训练可视', icon: 'chart' }
      },
      {
        path: 'label',
        component: () => import('@/views/model/label/index.vue'),
        name: 'ModelLabel',
        meta: { title: '模型标签', icon: 'edit' }
      }
    ]
  },
  // ====== 态势感知3D全景（隐藏路由，从首页进入） ======
  {
    path: '/digital-twin',
    component: Layout,
    hidden: true,
    children: [
      {
        path: 'overview',
        component: () => import('@/views/digitalTwin/index.vue'),
        name: 'DigitalTwin',
        meta: { title: '态势感知3D全景', icon: 'monitor' }
      }
    ]
  },
  // ====== 调度决策应用 ======
  {
    path: '/rag-scheduler',
    component: Layout,
    redirect: '/rag-scheduler/chat',
    name: 'RagScheduler',
    alwaysShow: true,
    meta: { title: '调度决策应用', icon: 'message' },
    children: [
      {
        path: 'chat',
        component: () => import('@/views/ragScheduler/chat.vue'),
        name: 'RagChat',
        meta: { title: '调度助手', icon: 'message' }
      },
      {
        path: 'knowledge',
        component: () => import('@/views/ragScheduler/knowledge.vue'),
        name: 'RagKnowledge',
        meta: { title: '知识库管理', icon: 'documentation' }
      },
      {
        path: 'simulation',
        component: () => import('@/views/digitalTwin/twinSimulation.vue'),
        name: 'TwinSimulation',
        meta: { title: '孪生推演', icon: 'chart' }
      },
      {
        path: 'decision-log',
        component: () => import('@/views/ragScheduler/decisionLog.vue'),
        name: 'DecisionLog',
        meta: { title: '决策日志', icon: 'log' }
      }
    ]
  },
  // ====== 攻防验证平台 ======
  {
    path: '/security',
    component: Layout,
    redirect: '/security/env-manage',
    name: 'Security',
    alwaysShow: true,
    meta: { title: '攻防验证平台', icon: 'bug' },
    children: [
      {
        path: 'env-manage',
        component: () => import('@/views/securityPlatform/envManage.vue'),
        name: 'EnvManage',
        meta: { title: '资源与环境管理', icon: 'clipboard' }
      },
      {
        path: 'attack-verify',
        component: () => import('@/views/securityPlatform/attackVerify.vue'),
        name: 'AttackVerify',
        meta: { title: '攻击验证与风险复现', icon: 'bug' }
      },
      {
        path: 'defense-config',
        component: () => import('@/views/securityPlatform/defenseConfig.vue'),
        name: 'DefenseConfig',
        meta: { title: '防御配置与效果评测', icon: 'checkcode' }
      },
      {
        path: 'situation-aware',
        component: () => import('@/views/securityPlatform/situationAware.vue'),
        name: 'SituationAware',
        meta: { title: '态势感知与预案生成', icon: 'eye-open' }
      },
      {
        path: 'evaluation',
        component: () => import('@/views/securityPlatform/evaluation.vue'),
        name: 'Evaluation',
        meta: { title: '效果评测与复盘分析', icon: 'chart' }
      }
    ]
  }
]

// 动态路由，基于用户权限动态去加载
export const dynamicRoutes = [
  {
    path: '/system/user-auth',
    component: Layout,
    hidden: true,
    permissions: ['system:user:edit'],
    children: [
      {
        path: 'role/:userId(\\d+)',
        component: () => import('@/views/system/user/authRole'),
        name: 'AuthRole',
        meta: { title: '分配角色', activeMenu: '/system/user' }
      }
    ]
  },
  {
    path: '/system/role-auth',
    component: Layout,
    hidden: true,
    permissions: ['system:role:edit'],
    children: [
      {
        path: 'user/:roleId(\\d+)',
        component: () => import('@/views/system/role/authUser'),
        name: 'AuthUser',
        meta: { title: '分配用户', activeMenu: '/system/role' }
      }
    ]
  },
  {
    path: '/system/dict-data',
    component: Layout,
    hidden: true,
    permissions: ['system:dict:list'],
    children: [
      {
        path: 'index/:dictId(\\d+)',
        component: () => import('@/views/system/dict/data'),
        name: 'Data',
        meta: { title: '字典数据', activeMenu: '/system/dict' }
      }
    ]
  },
  {
    path: '/monitor/job-log',
    component: Layout,
    hidden: true,
    permissions: ['monitor:job:list'],
    children: [
      {
        path: 'index/:jobId(\\d+)',
        component: () => import('@/views/monitor/job/log'),
        name: 'JobLog',
        meta: { title: '调度日志', activeMenu: '/monitor/job' }
      }
    ]
  },
  {
    path: '/tool/gen-edit',
    component: Layout,
    hidden: true,
    permissions: ['tool:gen:edit'],
    children: [
      {
        path: 'index/:tableId(\\d+)',
        component: () => import('@/views/tool/gen/editTable'),
        name: 'GenEdit',
        meta: { title: '修改生成配置', activeMenu: '/tool/gen' }
      }
    ]
  },
  {
    path: '/modelmag/model-line-detail',
    component: Layout,
    hidden: true,
    permissions: ['modelmag:model-line:list'],
    children: [
      {
        path: 'index/:modelId(\\d+)',
        name: 'model-line-detail',
        component: () => import('@/views/model/line/pages/detail.vue'),
        meta: {title: '模型产线详情',activeMenu: '/modelmag/model-line'}
      }
    ]
  },
  {
    path: '/modelmag/model-detail',
    component: Layout,
    hidden: true,
    permissions: ['modelmag:model:list'],
    children: [
      {
        path: 'index/:modelId(\\d+)',
        name: 'model-detail',
        component: () => import('@/views/model/modelMag/pages/detail.vue'),
        meta: { title: '模型详情', activeMenu: '/modelmag/model'}
      }
    ]
  },
  {
    path: '/datamag/dataset-detail',
    component: Layout,
    hidden: true,
    permissions: ['datamag:dataset:list'],
    children: [
      {
        path: 'index/:detailId(\\d+)',
        name: 'dataset-detail',
        component: () => import('@/views/dataset/datasetDetail.vue'),
        meta: { title: '数据集明细', activeMenu: '/datamag/dataset'}
      }
    ]
  },

  {
    path: '/datamag/mark-task-add',
    component: Layout,
    hidden: true,
    permissions: ['datamag:mark-task:add'],
    children: [
      {
        path: 'task-create',
        name: 'task-create',
        component: () => import('@/views/dataset/dataMark/createMarkTask.vue'),
        meta: {title: '新增标注任务', activeMenu: '/datamag/mark-task' }
      }
    ]
  },
  {
    path: '/datamag/mark-category', // 分类标注
    component: Layout,
    hidden: true,
    permissions: ['datamag:mark-task:mark'],
    children: [
      {
        path: 'mark-by-classification/:taskId(\\d+)',
        name: 'mark-by-classification',
        component: () => import('@/views/dataset/dataMark/mark1.vue'),
        meta: {title: '图片分类标注', activeMenu: '/datamag/mark-task' }
      }
    ]
  },
  {
    path: '/datamag/mark-detection', // 目标检测标注
    component: Layout,
    hidden: true,
    permissions: ['datamag:mark-task:mark'],
    children: [
      {
        path: 'mark-by-obj-detection/:taskId(\\d+)',
        name: 'mark-by-obj-detection',
        component: () => import('@/views/dataset/dataMark/detectionMarkControl.vue'),
        meta: {title: '目标检测标注', activeMenu: '/datamag/mark-task' }
      }
    ]
  },
  {
    path: '/datamag/mark-text', // 文本标注
    component: Layout,
    hidden: true,
    permissions: ['datamag:mark-task:mark'],
    children: [
      {
        path: 'mark-by-text/:taskId(\\d+)',
        name: 'mark-by-text',
        component: () => import('@/views/dataset/dataMark/textMark.vue'),
        meta: {title: '文本标注', activeMenu: '/datamag/mark-task' }
      }
    ]
  },
  {
    path: '/datamag/mark-table', // 表格标注
    component: Layout,
    hidden: true,
    permissions: ['datamag:mark-task:mark'],
    children: [
      {
        path: 'mark-by-table/:taskId(\\d+)',
        name: 'mark-by-table',
        component: () => import('@/views/dataset/dataMark/tableMark.vue'),
        meta: {title: '表格标注', activeMenu: '/datamag/mark-task' }
      }
    ]
  },
  {
    path: '/modelmag/model-line-add',
    component: Layout,
    hidden: true,
    permissions: ['modelmag:model-line:add'],
    children: [
      {
        path: 'line-create/:lineId',
        name: 'line-create',
        component: () => import('@/views/model/line/pages/createModelLine.vue'),
        meta: {title: '模型产线新建', activeMenu: '/modelmag/model-line' }
      }
    ]
  },
  {
    path: '/modelmag/model-line-orchestration',
    component: Layout,
    hidden: true,
    permissions: ['modelmag:model-line:add'],
    children: [
      {
        path: 'orchestration/:lineId',
        name: 'model-line-orchestration',
        component: () => import('@/views/model/line/pages/orchestration.vue'),
        meta: {title: '模型产线编排', activeMenu: '/modelmag/model-line' }
      }
    ]
  },
  {
    path: '/modelmag/model-add',
    component: Layout,
    hidden: true,
    permissions: ['modelmag:model:add'],
    children: [
      {
        path: 'model-create/:id?/:secsceneid?/:sceneid?',
        name: 'model-create',
        component: () => import('@/views/model/modelMag/pages/createModel.vue'),
        meta: {title: '模型新建', activeMenu: '/modelmag/model' }
      }
    ]

  },
  {
    path: '/modelmag/service-detail',
    component: Layout,
    hidden: true,
    permissions: ['modelmag:service-list:list'],
    children: [
      {
        path: 'ser/:serviceId(\\d+)',
        name: 'service-detail',
        component: () => import('@/views/model/service/pages/detail.vue'),
        meta: { title: '服务详情', activeMenu: '/modelmag/service-list'}
      }
    ]
  },
  {
    path: '/modelmag/service-add',
    component: Layout,
    hidden: true,
    permissions: ['modelmag:service-list:add'],
    children: [
      {
        path: 'test',
        name: 'index',
        component: () => import('@/views/model/service/pages/createService.vue'),
        meta: { title: '新增服务',activeMenu: '/modelmag/service-list'}
      }
    ]
  },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes: constantRoutes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {

      // return savedPosition
      return { top: 0 }

    } else {
      return { top: 0 }
    }
  },
});

export default router;
