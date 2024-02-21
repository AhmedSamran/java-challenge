package jp.co.axa.apidemo.controllers;

import jp.co.axa.apidemo.entities.Employee;
import jp.co.axa.apidemo.services.EmployeeService;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(EmployeeController.class)
@AutoConfigureMockMvc(addFilters = false)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
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
    public void testGetEmployees() throws Exception {
        List<Employee> employees = Collections.singletonList(employee);
        given(employeeService.retrieveEmployees()).willReturn(employees);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].name", Matchers.is(employee.getName())));
    }

    @Test
    public void testGetEmployee() throws Exception {
        given(employeeService.getEmployee(1L)).willReturn(employee);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/employees/{employeeId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", Matchers.is(employee.getName())));
    }

    @Test
    public void testSaveEmployee() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"John Doe\",\"salary\":10000,\"department\":\"IT\"}"))
                .andExpect(status().isCreated());

        verify(employeeService, times(1)).saveEmployee(any(Employee.class));

    }

    @Test
    public void testDeleteEmployee() throws Exception {
        given(employeeService.deleteEmployee(1L)).willReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/employees/{employeeId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).deleteEmployee(1L);
    }

    @Test
    public void testUpdateEmployee() throws Exception {
        given(employeeService.getEmployee(1L)).willReturn(employee);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/employees/{employeeId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"John Doe Updated\",\"salary\":12000,\"department\":\"HR\"}"))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).updateEmployee(any(Employee.class));
    }


}
