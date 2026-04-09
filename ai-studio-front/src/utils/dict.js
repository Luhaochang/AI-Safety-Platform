import useDictStore from '@/store/modules/dict'
import { getDicts } from '@/api/system/dict/data'

/**
 * 获取字典数据
 */
export function useDict(...args) {
  const res = ref({});
  return (() => {
    args.forEach((dictType, index) => {
      res.value[dictType] = [];
      const dicts = useDictStore().getDict(dictType);
      if (dicts) {
        res.value[dictType] = dicts;
      } else {
        const resp = {
          "msg": "操作成功",
          "code": 200,
          "data": [
            {
              "createBy": "admin",
              "createTime": "2024-06-30 11:28:21",
              "updateBy": null,
              "updateTime": null,
              "remark": "正常状态",
              "dictCode": 6,
              "dictSort": 1,
              "dictLabel": "正常",
              "dictValue": "0",
              "dictType": "sys_normal_disable",
              "cssClass": "",
              "listClass": "primary",
              "isDefault": "Y",
              "status": "0",
              "default": true
            },
            {
              "createBy": "admin",
              "createTime": "2024-06-30 11:28:21",
              "updateBy": null,
              "updateTime": null,
              "remark": "停用状态",
              "dictCode": 7,
              "dictSort": 2,
              "dictLabel": "停用",
              "dictValue": "1",
              "dictType": "sys_normal_disable",
              "cssClass": "",
              "listClass": "danger",
              "isDefault": "N",
              "status": "0",
              "default": false
            }
          ]
        }
        // getDicts(dictType).then(resp => {
          res.value[dictType] = resp.data.map(p => ({ label: p.dictLabel, value: p.dictValue, elTagType: p.listClass, elTagClass: p.cssClass }))
          useDictStore().setDict(dictType, res.value[dictType]);
        // })
      }
    })
    return toRefs(res.value);
  })()
}
