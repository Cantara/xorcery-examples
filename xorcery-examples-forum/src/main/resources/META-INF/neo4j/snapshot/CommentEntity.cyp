MATCH (entity:Comment {id:$id})
RETURN entity.body as body
