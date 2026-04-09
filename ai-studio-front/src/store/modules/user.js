import {login, logout, getInfo, getPermission} from '@/api/login'
import {getToken, setToken, removeToken} from '@/utils/auth'
import defAva from '@/assets/images/profile.jpg'

const useUserStore = defineStore(
    'user',
    {
        state: () => ({
            token: getToken(),
            id: '',
            name: '',
            avatar: '',
            roles: [],
            permissions: []
        }),
        actions: {
            // 登录
            login(userInfo) {
                const username = userInfo.username.trim()
                const password = userInfo.password
                const code = userInfo.code
                const uuid = userInfo.uuid
                return new Promise((resolve, reject) => {
                    login(username, password, code, uuid).then(res => {
                        resolve()
                    }).catch(error => {
                        reject(error)
                    })
                })
            },
            // 获取用户信息
            getInfo() {
                return new Promise((resolve, reject) => {
                    getInfo().then(res => {

                        const {data} = res
                        const user = data.user
                        const avatar = (user.avatar === "" || user.avatar == null) ? defAva :  user.avatar;

                        getPermission().then(response => {
                            if (response.code === 200 ) {
                                if (response.data.roles && response.data.roles.length > 0) {
                                    const {permissions,roles} = response.data;
                                    this.roles = roles;
                                    this.permissions = permissions;
                                }

                                this.id = user.userId
                                this.name = user.userName
                                this.avatar = avatar
                                resolve(res)
                            }
                        })
                        // if (res.roles && res.roles.length > 0) { // 验证返回的roles是否是一个非空数组
                        //     this.roles = res.roles
                        //     this.permissions = res.permissions
                        // } else {
                        //     this.roles = ['ROLE_DEFAULT']
                        // }

                    }).catch(error => {
                        reject(error)
                    })
                })
            },
            // 退出系统
            logOut() {
                return new Promise((resolve, reject) => {
                    // logout(this.token).then(() => {
                        this.token = ''
                        this.roles = []
                        this.permissions = []
                        removeToken()
                        resolve()
                    // }).catch(error => {
                    //     reject(error)
                    // })
                })
            },
            setAvatar(url){
                return new Promise(resolve => {
                    this.avatar = url;
                    resolve()
                })
            }
        }
    })

export default useUserStore
