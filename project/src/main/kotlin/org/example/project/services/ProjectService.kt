package org.example.project.services

import org.example.project.BoardRepository
import org.example.project.EmployeeClient
import org.example.project.EmployeeRole
import org.example.project.FeignClientException
import org.example.project.OrganizationClient
import org.example.project.Project
import org.example.project.ProjectMapper
import org.example.project.ProjectNotFoundException
import org.example.project.ProjectRepository
import org.example.project.SecurityUtil
import org.example.project.UserNotAllowedToCreateProjectException
import org.example.project.dtos.ProjectCreateDto
import org.example.project.dtos.ProjectFullResponseDto
import org.example.project.dtos.ProjectShortResponseDto
import org.example.project.dtos.ProjectUpdateDto
import org.example.project.dtos.RequestEmployeeRole
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

interface ProjectService{
    fun create (dto: ProjectCreateDto)
    fun update (id: Long, dto: ProjectUpdateDto)
    fun delete (id: Long)
    fun getById (id: Long): ProjectFullResponseDto
    fun getAll(pageable: Pageable): Page<ProjectShortResponseDto>
    fun getAllByOrganizationId(organizationId: Long?, pageable: Pageable): Page<ProjectShortResponseDto>
    fun close(id: Long)
}

@Service
class ProjectServiceImpl(
    private val repository: ProjectRepository,
    private val mapper: ProjectMapper,
    private val organizationClient: OrganizationClient,
    private val boardRepository: BoardRepository,
    private val employeeClient: EmployeeClient,
    private val securityUtil: SecurityUtil,
): ProjectService {
    override fun create(dto: ProjectCreateDto) {
        try{
            val organization = organizationClient.getCurrentUserOrganization(securityUtil.getCurrentUserId())
            val employeeRole=employeeClient.getEmployeeRoleByUserId(securityUtil.getCurrentUserId(),RequestEmployeeRole(securityUtil.getCurrentUserId(), organization.organizationId))
            if(employeeRole.employeeRole != EmployeeRole.CEO){
                throw UserNotAllowedToCreateProjectException()
            }
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

    override fun getAllByOrganizationId(
        organizationId: Long?,
        pageable: Pageable
    ): Page<ProjectShortResponseDto> {
        if(organizationId == null){
            val organization = organizationClient.getCurrentUserOrganization(securityUtil.getCurrentUserId())
            return repository.findAllByOrganizationIdAndDeletedFalse(organization.organizationId, pageable).map { mapper.toShortDto(it) }
        }
        return repository.findAllByOrganizationIdAndDeletedFalse(organizationId, pageable).map { mapper.toShortDto(it) }
    }

    override fun close(id: Long) {
        val project = repository.findByIdAndDeletedFalse(id)
            ?: throw ProjectNotFoundException()
        
        project.isActive = false
        project.endDate = java.time.LocalDate.now()
        repository.save(project)
    }
}