package uk.ac.qub.qubcoin.models;

import org.junit.Test;

import static org.junit.Assert.*;

public class ModuleTest {

    @Test
    public void testCombineModuleCodes() {
        String courseCode = "CSC";
        String moduleCode = "3002";
        assertEquals(Module.combineModuleCodes(courseCode, moduleCode), "CSC3002");
    }
}