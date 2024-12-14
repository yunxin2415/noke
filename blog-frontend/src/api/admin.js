import request from '@/utils/request'

export function getUsers() {
  return request({
    url: '/admin/users',
    method: 'get'
  })
}

export function deleteUser(userId) {
  return request({
    url: `/admin/users/${userId}`,
    method: 'delete'
  })
}

export function updateUser(userId, data) {
  return request({
    url: `/admin/users/${userId}`,
    method: 'put',
    data
  })
}

export function changeUserRole(userId, role) {
  return request({
    url: `/admin/users/${userId}/role`,
    method: 'put',
    data: { role }
  })
} 