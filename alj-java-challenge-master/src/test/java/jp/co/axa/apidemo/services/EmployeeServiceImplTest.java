package jp.co.axa.apidemo.services;

import jp.co.axa.apidemo.entities.Employee;
import jp.co.axa.apidemo.repositories.EmployeeRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration
public class EmployeeServiceImplTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Autowired
    CacheManager cacheManager;
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private EmployeeServiceImpl employeeServiceImpl;
    @Autowired
    private EmployeeService employeeService;
    private Employee employee;

    @Before
    public void setup() {
        employee = new Employee();
        employee.setId(1L);
        employee.setName("John Doe");
        employee.setSalary(10000);
        employee.setDepartment("IT");
    }

    @Test
    public void testRetrieveEmployees() {
        given(employeeRepository.findAll()).willReturn(Collections.singletonList(employee));

        List<Employee> result = employeeServiceImpl.retrieveEmployees();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo(employee.getName());
    }

    @Test
    public void testGetEmployee() {
        given(employeeRepository.findById(1L)).willReturn(Optional.of(employee));

        Employee result = employeeServiceImpl.getEmployee(1L);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(employee.getName());
    }

    @Test
    public void testSaveEmployee() {
        employeeServiceImpl.saveEmployee(employee);

        then(employeeRepository).should().save(employee);
    }

    @Test
    public void testDeleteEmployee_ExistingEmployee() {
        given(employeeRepository.existsById(1L)).willReturn(true);

        boolean result = employeeServiceImpl.deleteEmployee(1L);

        assertThat(result).isTrue();
        then(employeeRepository).should().deleteById(1L);
    }

    @Test
    public void testDeleteEmployee_NonExistingEmployee() {

        given(employeeRepository.existsById(1L)).willReturn(false);

        boolean result = employeeServiceImpl.deleteEmployee(1L);

        assertThat(result).isFalse();
        then(employeeRepository).should(BDDMockito.never()).deleteById(any());
    }

    @Test
    public void testUpdateEmployee_ExistingEmployee() {

        given(employeeRepository.existsById(1L)).willReturn(true);

        employeeServiceImpl.updateEmployee(employee);

        then(employeeRepository).should().save(employee);
    }

    @Test
    public void testUpdateEmployee_NonExistingEmployee() {
        given(employeeRepository.existsById(1L)).willReturn(false);
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Employee with ID 1 does not exist.");
        employeeServiceImpl.updateEmployee(employee);
    }

    @Test
    public void whenGetEmployeeTwice_thenSecondCallShouldUseCache() {

        employeeService.saveEmployee(employee);

        Employee firstCall = employeeService.getEmployee(1L);

        Employee secondCall = employeeService.getEmployee(1L);

        Collection<String> caches = cacheManager.getCacheNames();

        assertEquals(firstCall, secondCall);

        assertFalse(cacheManager.getCacheNames().isEmpty());

        assertNotNull(caches.stream().findFirst());
    }

    @Test
    public void whenDeleteEmployee_thenCacheShouldBeEvicted() {
        given(employeeRepository.findById(anyLong())).willReturn(Optional.of(employee));
        given(employeeRepository.existsById(anyLong())).willReturn(true);
        doNothing().when(employeeRepository).deleteById(anyLong());

        employeeServiceImpl.getEmployee(1L);

        employeeServiceImpl.deleteEmployee(1L);


        employeeServiceImpl.getEmployee(1L);

        verify(employeeRepository, times(2)).findById(1L);
    }

    @Configuration
    @EnableCaching
    static class TestConfig {
        @Bean
        public EmployeeService employeeService(EmployeeRepository employeeRepository) {
            return new EmployeeServiceImpl(employeeRepository);
        }

        @Bean
        public EmployeeRepository employeeRepository() {
            return mock(EmployeeRepository.class);
        }

        @Bean
        public CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("employees", "employee");
        }
    }
}