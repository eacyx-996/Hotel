import { defineStore } from 'pinia'
import request from '@/utils/request'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null'),
  }),
  actions: {
    async login(username, password) {
      const res = await request.post('/auth/login', { username, password })
      this.token = res.data.token
      this.userInfo = res.data.user
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('userInfo', JSON.stringify(res.data.user))
      return res.data
    },
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    },
  },
})
