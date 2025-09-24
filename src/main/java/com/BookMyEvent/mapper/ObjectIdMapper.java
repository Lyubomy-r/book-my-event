package com.BookMyEvent.mapper;

import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ObjectIdMapper {
    @Named("objectIdToString")
    default String asString(ObjectId id) {
        return id != null ? id.toHexString() : null;
    }

    @Named("stringToObjectId")
    default ObjectId asObjectId(String id) {
        return id != null ? new ObjectId(id) : null;
    }
}
