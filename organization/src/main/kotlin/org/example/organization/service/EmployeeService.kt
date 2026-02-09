package org.example.organization.service

import org.example.organization.EmployeeAlreadyExistsException
import org.example.organization.EmployeeContext
import org.example.organization.EmployeeContextRepository
import org.example.organization.EmployeeMapper
import org.example.organization.EmployeeNotFoundException
import org.example.organization.EmployeeRepository
import org.example.organization.OrganizationNotActiveException
import org.example.organization.OrganizationNotFoundException
import org.example.organization.OrganizationRepository
import org.example.organization.SecurityUtil
import org.example.organization.dto.EmployeeCreateRequest
import org.example.organization.dto.EmployeeResponse
import org.example.organization.dto.EmployeeRoleResponse
import org.example.organization.dto.EmployeeRoleUpdateRequest
import org.example.organization.dto.EmployeeUpdateRequest
import org.example.organization.dto.RequestEmployeeRole
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface EmployeeService {
    fun addEmployee(organizationId: Long, body: EmployeeCreateRequest, createdByUserId: Long?)
    fun getEmployeesByOrganization(organizationId: Long): List<EmployeeResponse>
    fun updateEmployee(organizationId: Long, userId: Long, body: EmployeeUpdateRequest)
    fun removeEmployee(organizationId: Long, userId: Long)
    fun getEmployeeRole(body: RequestEmployeeRole): EmployeeRoleResponse
    fun updateEmployeeRole(organizationId: Long, userId: Long, body: EmployeeRoleUpdateRequest)
    fun areAllUsersInOrganization(organizationId: Long, userIds: List<Long>): Boolean
}

@Service
class EmployeeServiceImpl(
    private val employeeRepository: EmployeeRepository,
    private val employeeContextRepo: EmployeeContextRepository,
    private val organizationRepository: OrganizationRepository,
    private val mapper: EmployeeMapper,
    private val securityUtil: SecurityUtil,
) : EmployeeService {

    @Transactional
    override fun addEmployee(organizationId: Long, body: EmployeeCreateRequest, createdByUserId: Long?) {
        val org = organizationRepository.findByIdAndDeletedFalse(organizationId)
            ?: throw OrganizationNotFoundException()

        if (!org.isActive) throw OrganizationNotActiveException()

        // duplicate membership
        if (employeeRepository.existsByUserIdAndOrganizationId(body.userId, organizationId)) {
            throw EmployeeAlreadyExistsException()
        }

        val contextEmployee = employeeRepository.save(mapper.toEntity(body, org, createdByUserId))

        employeeContextRepo.existsEmployeeContextByUserId(body.userId).takeIf { it }?.let {
            return
        }

        employeeContextRepo.save(EmployeeContext(contextEmployee.userId, org))
    }

    override fun getEmployeesByOrganization(organizationId: Long): List<EmployeeResponse> {
        organizationRepository.findByIdAndDeletedFalse(organizationId) ?: throw OrganizationNotFoundException()

        return employeeRepository.findAllByOrganizationIdAndDeletedFalse(organizationId)
            .map { mapper.toResponse(it) }
    }


    @Transactional
    override fun updateEmployee(organizationId: Long, userId: Long, body: EmployeeUpdateRequest) {
        organizationRepository.findByIdAndDeletedFalse(organizationId) ?: throw OrganizationNotFoundException()

        val employee = employeeRepository.findByUserIdAndOrganizationIdAndDeletedFalse(userId, organizationId)
            ?: throw EmployeeNotFoundException()

        body.position?.let { employee.position = it }
        body.department?.let { employee.department = it }
        body.isActive?.let { employee.isActive = it }

        employeeRepository.save(employee)
    }

    @Transactional
    override fun removeEmployee(organizationId: Long, userId: Long) {
        organizationRepository.findByIdAndDeletedFalse(organizationId) ?: throw OrganizationNotFoundException()

        val employee = employeeRepository.findByUserIdAndOrganizationIdAndDeletedFalse(userId, organizationId)
            ?: throw EmployeeNotFoundException()

        employeeRepository.trash(employee.id!!)
    }

    override fun getEmployeeRole(body: RequestEmployeeRole): EmployeeRoleResponse {
         val employee =employeeRepository.findByUserIdAndOrganizationIdAndDeletedFalse(body.userId, body.organizationId)
        ?:throw EmployeeNotFoundException()
        return EmployeeRoleResponse(employee.employeeRole)
    }

    override fun updateEmployeeRole(
        organizationId: Long,
        userId: Long,
        body: EmployeeRoleUpdateRequest
    ) {
        organizationRepository.findByIdAndDeletedFalse(organizationId)
            ?: throw OrganizationNotFoundException()

        val employee = employeeRepository
            .findByUserIdAndOrganizationIdAndDeletedFalse(userId, organizationId)
            ?: throw EmployeeNotFoundException()

        employee.employeeRole = body.employeeRole
        employeeRepository.save(employee)
    }

    override fun areAllUsersInOrganization(
        organizationId: Long,
        userIds: List<Long>
    ): Boolean {
        val count = employeeRepository.countEmployeesInOrganization(organizationId, userIds)
        if (count != userIds.size.toLong()) {
            throw EmployeeNotFoundException()
        }

        return true
    }
}
