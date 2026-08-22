interface TagBadgeListProps {
  tags: string[]
}

export function TagBadgeList({ tags }: TagBadgeListProps) {
  if (tags.length === 0) return null

  return (
    <div className="flex flex-wrap gap-1">
      {tags.map((tag) => (
        <span key={tag} className="badge badge-sm badge-outline">
          {tag}
        </span>
      ))}
    </div>
  )
}
