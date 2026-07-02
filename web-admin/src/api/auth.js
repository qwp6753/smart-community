import request from './request'

export const login = (data) => request.post('/auth/login', data)
export const currentUser = () => request.get('/auth/current-user')
