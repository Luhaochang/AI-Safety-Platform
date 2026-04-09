import request from "@/utils/request.js";

// 分页
export function listTask(params) {
    return request({
        method: 'get',
        url: 'production-line/data-set-task/condition',
        params
    })
}


// 新增标注任务
export function addTask(data) {
    return request({
        method: 'post',
        url: 'production-line/data-set-task',
        data: data
    })
}

// 删除标注任务
export function deleteTask(params) {
    return request({
        method: 'delete',
        url: 'production-line/data-set-task',
        params
    })
}

// 根据任务id查询对象
export function getTaskById(id){
    return request({
        method: 'get',
        url: 'production-line/data-set-task/by-id',
        params: {
            id: id
        }
    })
}

// 保存YOLO格式标注任务
export function savaTask(formData) {
    return request({
        method: 'post',
        url: 'production-line/data-set-task/save',
        data: formData,
        headers: {'Content-Type':'multipart/form-data'}
    })
}

// 提交YOLO格式标注任务
export function submitTask(formData) {
    return request({
        method: 'post',
        url: 'production-line/data-set-task/submit',
        data: formData,
        headers: {'Content-Type':'multipart/form-data'}
    })
}

// return request.post('production-line/resources/upload-file',data,{headers: {'Content-Type':'multipart/form-data'},onUploadProgress})
