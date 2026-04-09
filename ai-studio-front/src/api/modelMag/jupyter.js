import request from "@/utils/request.js";

// 查询jupyter可用镜像
export function getJupyterImage() {
    return request({
        method: "get",
        url: 'production-line/jupyter-service/jupyter-image'
    })
}

// 部署jupyter镜像
export function deployJupyterImage(data) {
    return request({
        method: 'post',
        url:'production-line/jupyter-service',
        data: data
    })
}

// 删除jupyter镜像
export function deleteJupyterImage(params) {
    return request({
        method: 'delete',
        url: 'production-line/jupyter-service',
        params:params
    })
}

//  根据绑定模型id查询jupyter服务
export function getJupyterServiceById(params) {
    return request({
        url: 'production-line/jupyter-service/jupyter-by-model',
        method: 'get',
        params: params
    })
}


