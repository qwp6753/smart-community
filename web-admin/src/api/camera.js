import request from './request'

export const listCameras = (params) => request.get('/cameras', { params })
export const getCamera = (id) => request.get(`/cameras/${id}`)
export const createCamera = (data) => request.post('/cameras', data)
export const updateCamera = (id, data) => request.put(`/cameras/${id}`, data)
export const deleteCamera = (id) => request.delete(`/cameras/${id}`)
