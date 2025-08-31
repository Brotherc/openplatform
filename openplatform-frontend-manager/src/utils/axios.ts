import axios from 'axios'
import { Modal, message } from 'ant-design-vue'

// 设置API基础URL
axios.defaults.baseURL = 'http://127.0.0.1:8080'

// 设置请求拦截器，自动添加Authorization请求头
axios.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('access_token')
        if (token) {
            config.headers.Authorization = token
        }
        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

// 清除用户信息
const clearUserInfo = () => {
    localStorage.removeItem('access_token')
    localStorage.removeItem('userInfo')
}

// 跳转到登录页
const redirect = () => {
    window.location.href = '/login'
}

// 清除用户信息并跳转到登录页
const clearUserInfoAndRedirect = () => {
    clearUserInfo()
    redirect()
}

// 设置响应拦截器，处理token过期等情况
axios.interceptors.response.use(
    (response) => {
        // 检查响应数据中的code
        if (response.data && response.data.code) {
            const code = response.data.code

            if (code === 1111011) {
                // 直接跳转到登录页
                clearUserInfoAndRedirect()
                return Promise.reject(new Error('需要重新登录'))
            } else if (code === 1111012) {
                clearUserInfo()
                // 显示弹窗提示长时间未操作
                Modal.confirm({
                    title: '提示',
                    content: '长时间未操作，请重新登录',
                    okText: '确定',
                    cancelText: '取消',
                    onOk: () => {
                        redirect()
                    },
                    onCancel: () => {
                        redirect()
                    }
                })
                return Promise.reject(new Error('长时间未操作'))
            } else if (code === 1010115) {
                clearUserInfo()
                // 显示弹窗提示用户不存在
                Modal.confirm({
                    title: '提示',
                    content: '用户不存在，请重新登录',
                    okText: '确定',
                    cancelText: '取消',
                    onOk: () => {
                        redirect()
                    },
                    onCancel: () => {
                        redirect()
                    }
                })
                return Promise.reject(new Error('用户不存在'))
            } else if (code === 1010116) {
                clearUserInfo()
                // 显示弹窗提示用户状态异常
                Modal.confirm({
                    title: '提示',
                    content: '用户状态异常，请重新登录',
                    okText: '确定',
                    cancelText: '取消',
                    onOk: () => {
                        redirect()
                    },
                    onCancel: () => {
                        redirect()
                    }
                })
                return Promise.reject(new Error('用户状态异常'))
            }
        }

        return response
    },
    (error) => {
        // 处理HTTP状态码错误
        if (error.response) {
            const status = error.response.status

            if (status === 401) {
                // HTTP 401状态码，token过期或无效
                clearUserInfoAndRedirect()
                return Promise.reject(error)
            } else if (status >= 500) {
                // 服务器错误
                message.error('服务器异常，请稍后重试')
            } else if (status === 404) {
                // 接口不存在
                message.error('请求的接口不存在')
            } else if (status === 403) {
                // 权限不足
                message.error('权限不足')
            } else {
                // 其他HTTP错误
                message.error('系统异常')
            }
        } else if (error.request) {
            // 网络错误
            message.error('网络连接异常，请检查网络设置')
        } else {
            // 其他错误
            message.error('系统异常')
        }

        return Promise.reject(error)
    }
)

export default axios
