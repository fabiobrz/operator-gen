package org.acme.read.crud;

import java.util.Optional;
import java.util.Map.Entry;

import org.eclipse.microprofile.openapi.models.PathItem;

public interface CrudMapper {

	Optional<Entry<String, PathItem>> getByIdPath();

	Optional<Entry<String, PathItem>> deletePath();

	Optional<Entry<String, PathItem>> createPath();

	Optional<Entry<String, PathItem>> patchPath();

}