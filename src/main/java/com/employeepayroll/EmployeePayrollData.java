package com.employeepayroll;

public class EmployeePayrollData {
    public int id;
    public String name;
    public double salary;

    //UC1 Employee Payroll Data
    public EmployeePayrollData(int id, String name, double salary) {
        this.id = id;
        this.name = name;
        this.salary = salary;
    }
    public String toString(){
        return "Employee_Name:"+name+", Employee_ID:"+id+", Employee_Salary:"+salary;
    }
}