import request from '@/utils/request'

// 获取路由
export const getRouters = () => {
    return request({
      url: 'auth/system/menu/routers',
      method: 'get'
    })

   /* return new Promise(resolve => {
        resolve({
            "msg": "操作成功",
            "code": 200,
            "data": [
                {
                    "name": "System",
                    "path": "/system",
                    "hidden": false,
                    "redirect": "noRedirect",
                    "component": "Layout",
                    "alwaysShow": true,
                    "meta": {
                        "title": "系统管理",
                        "icon": "system",
                        "noCache": false,
                        "link": null
                    },
                    "children": [
                        {
                            "name": "User",
                            "path": "user",
                            "hidden": false,
                            "component": "system/user/index",
                            "meta": {
                                "title": "用户管理",
                                "icon": "user",
                                "noCache": false,
                                "link": null
                            }
                        },
                        {
                            "name": "Role",
                            "path": "role",
                            "hidden": false,
                            "component": "system/role/index",
                            "meta": {
                                "title": "角色管理",
                                "icon": "peoples",
                                "noCache": false,
                                "link": null
                            }
                        },
                    ]
                },
                {
                    "name": "ModelMag",
                    "path": "/modelmag",
                    "hidden": false,
                    "redirect": "noRedirect",
                    "component": "Layout",
                    "alwaysShow": true,
                    "meta": {
                        "title": "模型管理",
                        "icon": "monitor",
                        "noCache": false,
                        "link": null
                    },
                    "children": [
                        {
                            "name": "Model-line",
                            "path": "model-line",
                            "hidden": false,
                            "component": "model/line/index",
                            "meta": {
                                "title": "模型产线",
                                "icon": "tree",
                                "noCache": false,
                                "link": null
                            }
                        },
                        {
                            "name": "Model",
                            "path": "model",
                            "hidden": false,
                            "component": "tool/build/index",
                            "meta": {
                                "title": "模型空间",
                                "icon": "build",
                                "noCache": false,
                                "link": null
                            }
                        },
                        {
                            "name": "Model-create",
                            "path": "model-create",
                            "hidden": true,
                            "component": "tool/gen/index",
                            "meta": {
                                "title": "模型生成",
                                "icon": "code",
                                "noCache": false,
                                "link": null
                            }
                        },
                        {
                            "name": "Model-detail",
                            "path": "model-detail",
                            "hidden": true,
                            "component": "tool/swagger/index",
                            "meta": {
                                "title": "模型详情",
                                "icon": "system",
                                "noCache": false,
                                "link": null
                            }
                        },
                        {
                            "name": "Service-list",
                            "path": "service-list",
                            "hidden": false,
                            "component": "model/service/modelList",
                            "meta": {
                                "title": "服务列表",
                                "icon": "system",
                                "noCache": false,
                                "link": null
                            }
                        }
                    ]
                },

                {
                    "name": "DataMag",
                    "path": "/datamag",
                    "hidden": false,
                    "redirect": "noRedirect",
                    "component": "Layout",
                    "alwaysShow": true,
                    "meta": {
                        "title": "数据管理",
                        "icon": "redis",
                        "noCache": false,
                        "link": null
                    },
                    "children": [
                        {
                            "name": "Dataset",
                            "path": "dataset",
                            "hidden": false,
                            "component": "dataset/index",
                            "meta": {
                                "title": "数据集",
                                "icon": "list",
                                "noCache": false,
                                "link": null
                            }
                        },
                        {
                            "name": "Dataset-mark",
                            "path": "dataset-mark",
                            "hidden": true,
                            // "component": "dataset/dataMark/mark",
                            "component": "dataset/dataMark/mark1",
                            "meta": {
                                "title": "分类类型标注操作台",
                                "icon": "code",
                                "noCache": false,
                                "link": null
                            }
                        },
                        {
                            "name": "Dataset-mark-detection",
                            "path": "dataset-mark-detection",
                            "hidden": true,
                            "component": "dataset/dataMark/detectionMarkControl",
                            "meta": {
                                "title": "检测类型标注操作台",
                                "icon": "code",
                                "noCache": false,
                                "link": null
                            }
                        },
                        {
                            "name": "Task-create",
                            "path": "task-create",
                            "hidden": true,
                            "component": "dataset/dataMark/createMarkTask",
                            "meta": {
                                "title": "新增标注任务",
                                "icon": "system",
                                "noCache": false,
                                "link": null
                            }
                        },
                        {
                            "name": "Mark-task",
                            "path": "mark-task",
                            "hidden": false,
                            "component": "dataset/dataMark/index",
                            "meta": {
                                "title": "标注任务",
                                "icon": "code",
                                "noCache": false,
                                "link": null
                            }
                        }
                    ]
                },
            ]
        })
    })*/
}
