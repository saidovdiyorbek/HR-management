package org.example.organization

import org.example.organization.dto.OrganizationAllResponse
import org.example.organization.dto.OrganizationCreateRequest
import org.example.organization.dto.OrganizationFullResponse
import org.springframework.stereotype.Component

@Component
class OrganizationMapper {

    fun toEntity(body: OrganizationCreateRequest, createdByUserId: Long?): Organization =
        Organization(
            name = body.name.trim(),
            description = body.description,
            isActive = body.isActive,
            createdByUserId = createdByUserId
        )

    fun toAllResponse(entity: Organization): OrganizationAllResponse =
        OrganizationAllResponse(
            id = entity.id!!,
            name = entity.name,
            isActive = entity.isActive
        )

    fun toFullResponse(entity: Organization): OrganizationFullResponse =
        OrganizationFullResponse(
            id = entity.id!!,
            name = entity.name,
            description = entity.description,
            isActive = entity.isActive
        )
}
