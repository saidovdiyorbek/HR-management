package org.example.project.services

import org.example.project.BoardRepository
import org.example.project.FeignClientException
import org.example.project.OrganizationClient
import org.example.project.Project
import org.example.project.ProjectMapper
import org.example.project.ProjectNotFoundException
import org.example.project.ProjectRepository
import org.example.project.SecurityUtil
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
    private val organizationClient: OrganizationClient,
    private val boardRepository: BoardRepository,
    private val securityUtil: SecurityUtil,
): ProjectService {
    override fun create(dto: ProjectCreateDto) {
        try{
            val organization = organizationClient.getCurrentUserOrganization(securityUtil.getCurrentUserId())
            val project = mapper.toEntity(dto, java.time.LocalDate.now(), organization.organizationId)
            repository.save(project)

        }catch (e: FeignClientException){
            throw e
        }
    }

    override fun update(id: Long, dto: ProjectUpdateDto) {
        val project = repository.findByIdAndDeletedFalse(id)
            ?: throw ProjectNotFoundException()
        dto.name?.let { project.name = it }
        dto.description?.let { project.description = it }
        dto.organizationId?.let {project.organizationId = it }
        repository.save(project)
    }

    override fun delete(id: Long) {
        repository.trash(id)
    }

    override fun getById(id: Long): ProjectFullResponseDto {
        val project = repository.findByIdAndDeletedFalse(id)
            ?: throw ProjectNotFoundException()
        return mapper.toFullDto(project, boardRepository.findByProjectIdAndDeletedFalse(id))
    }

    override fun getAll(pageable: Pageable): Page<ProjectShortResponseDto> {
        return repository.findAllNotDeleted(pageable).map { mapper.toShortDto(it) }
    }

    private fun getCurrentUserId():Long{
        TODO("get user id from security context it implements later")
    }
}