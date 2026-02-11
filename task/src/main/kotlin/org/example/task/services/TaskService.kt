package org.example.task.services

import jakarta.transaction.Transactional
import org.example.task.AttachClient
import org.example.task.EmployeeClient
import org.example.task.EmployeeRole
import org.example.task.FeignClientException
import org.example.task.GenerateHash
import org.example.task.OrganizationClient
import org.example.task.Permission
import org.example.task.ProjectClient
import org.example.task.Role
import org.example.task.SecurityUtil
import org.example.task.SomethingWentWrongException
import org.example.task.Task
import org.example.task.TaskAssignedEmployee
import org.example.task.TaskAssignedEmployeeRepository
import org.example.task.TaskAttachment
import org.example.task.TaskAttachmentRepository
import org.example.task.TaskNotFoundException
import org.example.task.TaskPriority
import org.example.task.TaskRepository
import org.example.task.ThisTaskIsNotYoursExceptions
import org.example.task.dtos.CheckUsersInOrganizationRequest
import org.example.task.dtos.InternalHashesCheckRequest
import org.example.task.dtos.RelationshipsCheckDto
import org.example.task.dtos.TaskCreateRequest
import org.example.task.dtos.TaskResponse
import org.example.task.dtos.TaskUpdateRequest
import org.example.task.dtos.TransferTaskCheckDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

interface TaskService {
    fun create(dto: TaskCreateRequest)
    fun getOne(id: Long): TaskResponse
    fun getAll(pageable: Pageable): Page<TaskResponse>
    fun update(id: Long, dto: TaskUpdateRequest)
    fun assignEmployee(id: Long, employees: List<Long>)
    fun unsignEmployee(id: Long, employees: List<Long>)
}

@Service
class TaskServiceImpl(
    private val repository: TaskRepository,
    private val taskAttachRepo: TaskAttachmentRepository,
    private val taskAssignedEmployeeRepo: TaskAssignedEmployeeRepository,
    private val security: SecurityUtil,

    private val attachClient: AttachClient,
    private val projectClient: ProjectClient,
    private val organizationClient: OrganizationClient,
    private val employeeClient: EmployeeClient,
    private val hash: GenerateHash,
    client: ProjectClient
) : TaskService {
    @Transactional
    override fun create(dto: TaskCreateRequest) {
        val currentUserId = security.getCurrentUserId()

        try {
            val checkTaskRelationshipsRes = projectClient.checkTaskRelationships(
                RelationshipsCheckDto(
                    dto.boardId, dto.stateId
                )
            )
            val currentOrganizationByUserId = organizationClient.getCurrentOrganizationByUserId(currentUserId)

            if (checkTaskRelationshipsRes.organizationId != currentOrganizationByUserId.organizationId){
                throw SomethingWentWrongException()
            }
            val savedTask = repository.save(Task(
                boardId = dto.boardId,
                stateId = dto.stateId,
                title = dto.title,
                taskNumber = hash.generateHash(),
                description = dto.description,
                priority = TaskPriority.LOW,
                orderIndex = repository.getTaskLastOrderIndex() ?: 1,
                estimatedHours = dto.estimatedHours,
                deadline = dto.deadline,
                tags = dto.tags,
                createUserId = currentUserId,
                currentOrganizationId = currentOrganizationByUserId.organizationId
            ))

            dto.assigningEmployeesId?.let {
                if (dto.assigningEmployeesId!!.isNotEmpty()) {
                    //TODO employeelar tekshiriladi
                    val savedAssigningEmployee: MutableList<TaskAssignedEmployee> = mutableListOf()
                    dto.assigningEmployeesId?.forEach { employeeId ->
                        savedAssigningEmployee.add(TaskAssignedEmployee(savedTask, employeeId, currentUserId))
                    }
                }
            }

            val savingTaskAttach: MutableList<TaskAttachment> = mutableListOf()
            dto.attachHashes?.let { attachHashes ->

                if (attachHashes.isNotEmpty()) {

                    attachClient.listExists(
                        InternalHashesCheckRequest(
                            security.getCurrentUserId(),
                            dto.attachHashes!!
                        )
                    )
                }

                attachHashes.forEach { attachHashFor ->
                    savingTaskAttach.add(TaskAttachment(
                        savedTask,
                        attachHashFor,
                    ))
                }
                taskAttachRepo.saveAll(savingTaskAttach)
            }

        }catch (e: FeignClientException){
            throw e
        }
    }

    override fun getOne(id: Long): TaskResponse {
        repository.findByIdAndDeletedFalse(id)?.let { task ->
            return TaskResponse(
                id = task.id!!,
                boardId = task.boardId,
                stateId = task.stateId,
                title = task.title,
                description = task.description,
                priority = task.priority,
                estimatedHours = task.estimatedHours,
                deadline = task.deadline,
                tags = task.tags,
                attachHashes = taskAttachRepo.findTaskAttachmentByTaskId(task.id!!),
                boarInfo = projectClient.getBoardUsers(task.boardId)
            )
        }
        throw TaskNotFoundException()
    }
    //TODO getAll roliga tekshirish, ozini ozi CEO qilish
    override fun getAll(pageable: Pageable): Page<TaskResponse> {
        val currentUserRole = security.getCurrentUserRole()
        val currentUserId = security.getCurrentUserId()

        if (currentUserRole == Role.DEVELOPER || currentUserRole == Role.ADMIN){
            val findAll = repository.findAll(pageable)
            return findAll.map { task ->
                TaskResponse(
                    id = task.id!!,
                    boardId = task.boardId,
                    stateId = task.stateId,
                    title = task.title,
                    description = task.description,
                    priority = task.priority,
                    estimatedHours = task.estimatedHours,
                    deadline = task.deadline,
                    tags = task.tags,
                    attachHashes = taskAttachRepo.findTaskAttachmentByTaskId(task.id!!),
                    boarInfo = projectClient.getBoardUsers(task.boardId)
                )
            }
        }

        //qaysi orgnizationda turgani
        val currentOrganizationByUserId = organizationClient.getCurrentOrganizationByUserId(currentUserId)
        //employeeni tasklari turgan organizationni ga qarab
        val employeeTasks = repository.getEmployeeTaskCurrentOrganization(
            currentOrganizationByUserId.organizationId,
            currentUserId, pageable
        )


            employeeTasks.forEach { task ->
                return employeeTasks.map { task ->
                    TaskResponse(
                        id = task.id!!,
                        boardId = task.boardId,
                        stateId = task.stateId,
                        title = task.title,
                        description = task.description,
                        priority = task.priority,
                        estimatedHours = task.estimatedHours,
                        deadline = task.deadline,
                        tags = task.tags,
                        attachHashes = taskAttachRepo.findTaskAttachmentByTaskId(task.id!!),
                        boarInfo = projectClient.getBoardUsers(task.boardId)
                        )
                }
            }
        return listOf<TaskResponse>() as Page<TaskResponse>
    }

    @Transactional
    override fun update(id: Long, dto: TaskUpdateRequest) {
        val currentUserId = security.getCurrentUserId()
        try{
            val employeeRole = employeeClient.getEmployeeRoleByUserId(currentUserId).employeeRole
            repository.findByIdAndDeletedFalse(id)?.let { task ->
                var permission: Permission = Permission.OWNER
                //taskni kim ozgartirmoqchi, Yaratgan employee yoki ceo qila oladi
                if(task.createUserId != currentUserId){
                    if (!taskAssignedEmployeeRepo.existsTaskAssignedEmployeeByTaskId(task.id!!)){
                        throw ThisTaskIsNotYoursExceptions()
                    }
                    permission = Permission.ASSIGNED
                }

                //complete statega faqat ceo oladi faqat shuni tekshiramiz
                dto.run {
                    this.stateId?.let { stateId ->
                        taskAssignedEmployeeRepo.existsTaskAssignedEmployeeByTaskId(task.id!!)


                        val checkTransferStates = projectClient.checkTransferStates(
                            TransferTaskCheckDto(
                                task.stateId,
                                stateId,
                                task.boardId,
                                permission
                            )
                        )
                        if (checkTransferStates){
                            task.stateId = stateId
                        }
                    }

                    this.title?.let { task.title = it }
                    this.description?.let { task.description = it }
                    this.priority?.let { task.priority = it }
                    this.estimatedHours?.let { task.estimatedHours = it }
                    this.deadline?.let { task.deadline = it }
                    this.tags?.let { task.tags = it }
                    if (this.attachHashes?.isNotEmpty() != false && dto.attachHashes != null) {
                        attachClient.listExists(InternalHashesCheckRequest(currentUserId, this.attachHashes!!))
                        this.attachHashes?.let { attachHash ->
                            val postAttachHashes = taskAttachRepo.getPostAttachHash(id)

                            val hashesToAdd = this.attachHashes!!.filter { !postAttachHashes.contains(it) }
                            val hashesToRemove = postAttachHashes.filter { !this.attachHashes!!.contains(it) }

                            if (hashesToRemove.isNotEmpty()) {
                                taskAttachRepo.removeByFileHashList(hashesToRemove)
                                attachClient.deleteList(hashesToRemove)
                            }

                            if (hashesToAdd.isNotEmpty()) {
                                attachClient.listExists(InternalHashesCheckRequest(currentUserId, hashesToAdd))
                                val postAttachesToAdd: MutableList<TaskAttachment> = mutableListOf()
                                hashesToAdd.forEach { hash ->
                                    postAttachesToAdd.add(TaskAttachment(task, hash))
                                }
                                taskAttachRepo.saveAll(postAttachesToAdd)
                            }
                        }
                        return
                    }
                    repository.save(task)
                    return
                }
            }
            throw TaskNotFoundException()
        }catch (e: FeignClientException){
            throw e
        }
    }
    @Transactional
    override fun assignEmployee(id: Long, employees: List<Long>) {
        val currentUserId = security.getCurrentUserId()
        try {
            repository.findByIdAndDeletedFalse(id)?.let { task ->
                if (task.createUserId != currentUserId){
                    throw ThisTaskIsNotYoursExceptions()
                }
                if(employees.isNotEmpty()){
                    val currentOrganizationByUserId = organizationClient.getCurrentOrganizationByUserId(currentUserId)
                    employeeClient.checkUsersInOrganization(CheckUsersInOrganizationRequest(currentOrganizationByUserId.organizationId, employees))
                    val assigningEmployees: MutableList<TaskAssignedEmployee> = mutableListOf()
                    employees.forEach { employee ->
                        assigningEmployees.add(TaskAssignedEmployee(task, employee, currentUserId))
                    }
                    taskAssignedEmployeeRepo.saveAll(assigningEmployees)
                }
                return
            }
            throw TaskNotFoundException()
        }catch (e: FeignClientException){
            throw e
        }
    }

    @Transactional
    override fun unsignEmployee(id: Long, employees: List<Long>) {
        val currentUserId = security.getCurrentUserId()
        try {
            repository.findByIdAndDeletedFalse(id)?.let { task ->
                if (task.createUserId != currentUserId){
                    throw ThisTaskIsNotYoursExceptions()
                }
                if(employees.isNotEmpty()){
                    //employeelar tekshirib keladi true bolsa keyingi qadam
                    val currentOrganizationByUserId = organizationClient.getCurrentOrganizationByUserId(currentUserId)
                    employeeClient.checkUsersInOrganization(CheckUsersInOrganizationRequest(currentOrganizationByUserId.organizationId, employees))
                    val oldAssignedEmployees = taskAssignedEmployeeRepo.findTaskAssignedEmployeeByTaskId(task.id!!)

                    val removeTo = employees.filter { oldAssignedEmployees.contains(it) }
                    taskAssignedEmployeeRepo.deleteTaskAssignedEmployeeByEmployeeIds(removeTo)
                }
                return
            }
        }catch (e: FeignClientException){
            throw e
        }
    }
}