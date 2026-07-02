import request from './request'

export const listCommunities = (params) => request.get('/communities', { params })
export const getCommunity = (id) => request.get(`/communities/${id}`)
export const createCommunity = (data) => request.post('/communities', data)
export const updateCommunity = (id, data) => request.put(`/communities/${id}`, data)
export const deleteCommunity = (id) => request.delete(`/communities/${id}`)
