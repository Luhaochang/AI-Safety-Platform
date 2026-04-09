import request from "@/utils/request.js";

// 根据应用场景查询
export function getChildScene(id) {
    return request({
        method: 'get',
        url: 'production-line/model/by-application',
        params: {
            applicationScene: id
        }
    })
}

// 根据任务场景查询 （5类）
export function getTaskSceneById(id) {
    return request({
        method: 'get',
        url: 'production-line/model/by-scene',
        params: {
            sceneId: id
        }
    })
}


export function getAllTaskType() {
    return request({
        method: 'get',
        url: 'production-line/task-type/all'
    })
}

export function getAllAppScene() {
    return request({
        method: 'get',
        url: 'production-line/app-scene/all'
    })
}
