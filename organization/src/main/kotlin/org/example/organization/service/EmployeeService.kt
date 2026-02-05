package org.example.organization.service

import org.example.organization.EmployeeAlreadyExistsException
import org.example.organization.EmployeeMapper
import org.example.organization.EmployeeNotFoundException
import org.example.organization.EmployeeRepository
import org.example.organization.EmployeeRole
import org.example.organization.OrganizationNotActiveException
import org.example.organization.OrganizationNotFoundException
import org.example.organization.OrganizationRepository
import org.example.organization.dto.EmployeeCreateRequest
import org.example.organization.dto.EmployeeResponse
import org.example.organization.dto.EmployeeUpdateRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface EmployeeService {
    fun addEmployee(organizationId: Long, body: EmployeeCreateRequest, createdByUserId: Long?)
    fun getEmployeesByOrganization(organizationId: Long): List<EmployeeResponse>
    fun getMyOrganizations(userId: Long): List<Long>
    fun updateEmployee(organizationId: Long, userId: Long, body: EmployeeUpdateRequest)
    fun removeEmployee(organizationId: Long, userId: Long)
}

@Service
class EmployeeServiceImpl(
    private val employeeRepository: EmployeeRepository,
    private val organizationRepository: OrganizationRepository,
    private val mapper: EmployeeMapper
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


        employeeRepository.save(mapper.toEntity(body, org, createdByUserId))
    }

    override fun getEmployeesByOrganization(organizationId: Long): List<EmployeeResponse> {
        organizationRepository.findByIdAndDeletedFalse(organizationId) ?: throw OrganizationNotFoundException()

        return employeeRepository.findAllByOrganizationIdAndDeletedFalse(organizationId)
            .map { mapper.toResponse(it) }
    }

    override fun getMyOrganizations(userId: Long): List<Long> {
        return employeeRepository.findAllByUserIdAndDeletedFalse(userId)
            .map { it.organization.id!! }
            .distinct()
    }

    @Transactional
    override fun updateEmployee(organizationId: Long, userId: Long, body: EmployeeUpdateRequest) {
        organizationRepository.findByIdAndDeletedFalse(organizationId) ?: throw OrganizationNotFoundException()

        val employee = employeeRepository.findByUserIdAndOrganizationIdAndDeletedFalse(userId, organizationId)
            ?: throw EmployeeNotFoundException()

        body.employeeRole?.let { newRole ->
            if (newRole == EmployeeRole.CEO && employee.employeeRole != EmployeeRole.CEO) {
                employeeRepository.findByOrganizationIdAndEmployeeRoleAndDeletedFalse(organizationId, EmployeeRole.CEO)
            }
            employee.employeeRole = newRole
        }

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

        // variant-1: soft delete (deleted=true)
        employeeRepository.trash(employee.id!!)

        // variant-2: faqat isActive=false (agar deleted ishlatmoqchi bo‘lmasangiz)
        // employee.isActive = false
        // employeeRepository.save(employee)
    }
}
