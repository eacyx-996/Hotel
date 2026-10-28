import request from '@/utils/request'

export const pageUsers = (params) => request.get('/user/page', { params })

export const createUser = (data) => request.post('/user', data)

export const updateUser = (data) => request.put('/user', data)

export const resetUserPassword = (data) => request.put('/user/reset-password', data)

export const deleteUser = (id) => request.delete(`/user/${id}`)
