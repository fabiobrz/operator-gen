package org.acme.read.crud;

import java.util.Optional;
import java.util.Map.Entry;

import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.media.Schema;

public interface CrudMapper {

	Optional<Entry<String, PathItem>> getByIdPath();

	Optional<Entry<String, PathItem>> deletePath();

	Optional<Entry<String, PathItem>> createPath();

	Optional<Entry<String, PathItem>> patchPath();

	Schema getByIdSchema();

	Optional<Schema> getCreateSchema();

	Optional<Schema> getUpdateSchema();

}