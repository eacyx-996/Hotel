import request from '@/utils/request'

export const pageRooms = (params) => request.get('/room/page', { params })

export const listRoomsWithType = () => request.get('/room/with-type')

export const listAvailableRooms = () => request.get('/room/available')

export const createRoom = (data) => request.post('/room', data)

export const updateRoom = (data) => request.put('/room', data)

export const updateRoomStatus = (id, status) => request.put(`/room/${id}/status`, null, { params: { status } })

export const deleteRoom = (id) => request.delete(`/room/${id}`)
