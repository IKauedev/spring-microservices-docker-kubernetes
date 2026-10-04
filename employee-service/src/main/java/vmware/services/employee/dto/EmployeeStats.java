package vmware.services.employee.dto;

import java.util.Map;

public record EmployeeStats(long total, double averageAge, Map<String, Long> byPosition) { }
