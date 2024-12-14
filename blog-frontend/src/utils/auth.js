export function isAdmin(user) {
  return user && user.role === 'ROLE_ADMIN'
}

export function canManageArticle(user, article) {
  return user && (user.role === 'ROLE_ADMIN' || user.id === article.author.id)
} 