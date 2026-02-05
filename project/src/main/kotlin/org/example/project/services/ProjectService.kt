package org.example.project.services

import org.example.project.BoardRepository
import org.example.project.OrganizationClient
import org.example.project.Project
import org.example.project.ProjectMapper
import org.example.project.ProjectRepository
import org.example.project.dtos.ProjectCreateDto
import org.example.project.dtos.ProjectFullResponseDto
import org.example.project.dtos.ProjectShortResponseDto
import org.example.project.dtos.ProjectUpdateDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

interface ProjectService{
    fun create (dto: ProjectCreateDto)
    fun update (id: Long, dto: ProjectUpdateDto)
    fun delete (id: Long)
    fun getById (id: Long): ProjectFullResponseDto
    fun getAll(pageable: Pageable): Page<ProjectShortResponseDto>
}

@Service
class ProjectServiceImpl(
    private val repository: ProjectRepository,
    private val mapper: ProjectMapper,
    private val organizationClient: OrganizationClient
): ProjectService {
    override fun create(dto: ProjectCreateDto) {
        val organization =organizationClient.getCurrentUserOrganization(getCurrentUserId())
        val project = mapper.toEntity(dto,  java.time.LocalDate.now(),organization.id )
        repository.save(project)
    }

    override fun update(id: Long, dto: ProjectUpdateDto) {

    }

    override fun delete(id: Long) {
        repository.trash(id)
    }

    override fun getById(id: Long): ProjectFullResponseDto {

    }

    override fun getAll(pageable: Pageable): Page<ProjectShortResponseDto> {
        TODO("Not yet implemented")
    }

    private fun getCurrentUserId():Long{
        TODO("get user id from security context")
    }
}