import request from '@/utils/request'

export const pageRoomTypes = (params) => request.get('/room-type/page', { params })

export const listRoomTypes = () => request.get('/room-type/list')

export const createRoomType = (data) => request.post('/room-type', data)

export const updateRoomType = (data) => request.put('/room-type', data)

export const deleteRoomType = (id) => request.delete(`/room-type/${id}`)
