import {getChildScene, getTaskSceneById} from "@/api/modelMag/scene.js";
import axios from "axios";
import { ref } from 'vue';

export const SecSceneOptions = ref([
    {
        label: '智慧城市',
        value: 1,
        leaf: false
    },
    {
        label: '工业/能源',
        value: 2,
        leaf: false

    },
    {
        label: '智慧安防',
        value: 3,
        leaf: false
    },
    {
        label: '智慧交通',
        value: 4,
        leaf: false
    },
    {
        label: '智慧金融',
        value: 5,
        leaf: false
    },
    {
        label: '互联网',
        value: 6,
        leaf: false
    },
    {
        label: '智慧零售',
        value: 7,
        leaf: false
    },
    {
        label: '智慧农业',
        value: 8,
        leaf: false
    }
])


export const cascaderProps = {
    // label: 'tagName',
    value: 'id',
    lazy: true,
    multiple: false,
    emitPath: false,

    lazyLoad(node, resolve) {
        console.log('lazyLoad triggered for node:', node);

        const { data } = node;
        if (!data || !data.value) {
            console.log('Invalid node data, resolving with empty array');
            return resolve([]);
        }

        const nodes = [];

        // 查询接口
        getChildScene(data.value)
            .then(res => {
                console.log('getChildScene response:', res);

                // 验证响应数据格式
                if (!res || !res.data || !Array.isArray(res.data)) {
                    console.error('Invalid response format:', res);
                    return resolve([]);
                }

                // 转换数据格式
                res.data.forEach(item => {
                    const obj = {
                        value: item.id,
                        label: item.secondarySceneName,
                        leaf: true, // 设置为true表示没有子节点
                        sceneId: item.sceneId
                    };
                    nodes.push(obj);
                });

                console.log('Resolving with nodes:', nodes);
                resolve(nodes);
            })
            .catch(error => {
                console.error('Error loading child nodes:', error);
                resolve([]); // 发生错误时返回空数组
            });
    },
}


export const rootExample = 'from mlflow.models import validate_serving_input\n' +
    '\n'+
    'model_uri = \'runs:/你的模型地址/model\'\n'+
    '\n' +
    '# The model is logged with an input example. MLflow converts\n' +
    '# it into the serving payload format for the deployed model endpoint,\n' +
    '# and saves it to \'serving_input_payload.json\'\n' +
    'serving_payload = """{\n' +
    '  "inputs": [\n' +
    '    [\n' +
    '      4.6,\n' +
    '      3.6,\n' +
    '      1.0,\n' +
    '      0.2\n' +
    '    ]\n' +
    '}"""\n' +
    '\n' +
    '# Validate the serving payload works on the model\n' +
    'validate_serving_input(model_uri, serving_payload)'
