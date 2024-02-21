package jp.co.axa.apidemo.services;

import jp.co.axa.apidemo.entities.Employee;
import jp.co.axa.apidemo.repositories.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    /**
     * Retrieves all employees from the repository.
     * Results are cached to improve performance.
     *
     * @return A list of all Employee entities.
     */
    @Override
    @Cacheable(value = "employees")
    public List<Employee> retrieveEmployees() {
        return employeeRepository.findAll();
    }

    /**
     * Retrieves an employee by their ID.
     * The result is cached to improve subsequent retrieval performance.
     *
     * @param employeeId The ID of the employee to retrieve.
     * @return The Employee entity if found, or {@code null} if not.
     */
    @Override
    @Cacheable(value = "employee", key = "#employeeId")
    public Employee getEmployee(Long employeeId) {
        Optional<Employee> optEmployee = employeeRepository.findById(employeeId);
        return optEmployee.orElse(null);
    }

    /**
     * Saves an Employee entity to the repository.
     * Evicts the entire 'employees' cache to ensure consistency.
     *
     * @param employee The Employee entity to save.
     */
    @Override
    @CacheEvict(value = "employees", allEntries = true)
    public void saveEmployee(Employee employee) {
        employeeRepository.save(employee);
    }

    /**
     * Deletes an Employee entity by their ID.
     * Evicts the specific employee and all 'employees' cache entries to ensure consistency.
     *
     * @param employeeId The ID of the employee to delete.
     * @return {@code true} if the employee was found and deleted, {@code false} otherwise.
     */
    @Override
    @CacheEvict(value = {"employee", "employees"}, key = "#employeeId", allEntries = true)
    public boolean deleteEmployee(Long employeeId) {
        if (employeeRepository.existsById(employeeId)) {
            employeeRepository.deleteById(employeeId);
            return true;
        }
        return false;
    }

    /**
     * Updates an existing Employee entity.
     * Evicts the 'employees' cache to ensure consistency.
     * Throws IllegalArgumentException if the employee does not exist.
     *
     * @param employee The Employee entity with updated information.
     * @throws IllegalArgumentException if the employee with the given ID does not exist.
     */
    @Override
    @CacheEvict(value = "employees", allEntries = true)
    public void updateEmployee(Employee employee) {
        if (!employeeRepository.existsById(employee.getId())) {
            throw new IllegalArgumentException("Employee with ID " + employee.getId() + " does not exist.");
        }
        employeeRepository.save(employee);
    }
}