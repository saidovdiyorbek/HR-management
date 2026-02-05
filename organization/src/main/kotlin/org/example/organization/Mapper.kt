package org.example.organization

import org.example.organization.dto.EmployeeCreateRequest
import org.example.organization.dto.EmployeeResponse
import org.example.organization.dto.OrganizationAllResponse
import org.example.organization.dto.OrganizationCreateRequest
import org.example.organization.dto.OrganizationFullResponse
import org.springframework.stereotype.Component
import java.time.LocalDateTime

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


@Component
class EmployeeMapper {

    fun toEntity(
        body: EmployeeCreateRequest,
        organization: Organization,
        createdByUserId: Long?
    ): Employee =
        Employee(
            userId = body.userId,
            organization = organization,
            employeeRole = body.employeeRole,
            position = body.position,
            department = body.department,
            isActive = true,
            joinedAt = LocalDateTime.now(),
            createdByUserId = createdByUserId
        )

    fun toResponse(e: Employee): EmployeeResponse =
        EmployeeResponse(
            id = e.id!!,
            userId = e.userId,
            organizationId = e.organization.id!!,
            employeeRole = e.employeeRole,
            position = e.position,
            department = e.department,
            isActive = e.isActive,
            joinedAt = e.joinedAt
        )
}

