package jp.co.axa.apidemo.controllers;

import jp.co.axa.apidemo.entities.Employee;
import jp.co.axa.apidemo.services.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * Retrieves a list of all employees.
     *
     * @return A ResponseEntity containing a list of employees.
     */
    @GetMapping("/employees")
    public ResponseEntity<List<Employee>> getEmployees() {
        List<Employee> employees = employeeService.retrieveEmployees();
        return ResponseEntity.ok().body(employees);
    }

    /**
     * Retrieves a single employee by their ID.
     *
     * @param employeeId The ID of the employee to retrieve.
     * @return A ResponseEntity containing the requested employee, or NotFound if the employee does not exist.
     */
    @GetMapping("/employees/{employeeId}")
    public ResponseEntity<Employee> getEmployee(@PathVariable(name = "employeeId") Long employeeId) {
        Employee employee = employeeService.getEmployee(employeeId);
        if (employee != null) {
            return ResponseEntity.ok().body(employee);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Saves a new employee to the system.
     *
     * @param employee The employee to save.
     * @return A ResponseEntity with a CREATED status if the employee is successfully saved.
     */
    @PostMapping("/employees")
    public ResponseEntity<Void> saveEmployee(@RequestBody Employee employee) {
        employeeService.saveEmployee(employee);
        log.info("Employee Saved Successfully");
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Deletes an employee by their ID.
     *
     * @param employeeId The ID of the employee to delete.
     * @return A ResponseEntity with NO_CONTENT status if the employee is successfully deleted, or NotFound if the employee does not exist.
     */
    @DeleteMapping("/employees/{employeeId}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable(name = "employeeId") Long employeeId) {
        boolean deleted = employeeService.deleteEmployee(employeeId);
        if (deleted) {
            log.info("Employee Deleted Successfully");
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Updates an existing employee's information.
     *
     * @param employee   The updated employee information.
     * @param employeeId The ID of the employee to update.
     * @return A ResponseEntity with NO_CONTENT status if the update is successful, or NotFound if the employee does not exist.
     */
    @PutMapping("/employees/{employeeId}")
    public ResponseEntity<Void> updateEmployee(@RequestBody Employee employee,
                                               @PathVariable(name = "employeeId") Long employeeId) {
        Employee existingEmployee = employeeService.getEmployee(employeeId);
        if (existingEmployee != null) {
            employeeService.updateEmployee(employee);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}