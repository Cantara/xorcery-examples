MATCH (entity:Comment {id:$entity_id})
RETURN entity.body as body
