package com.example.safeguardapp.Group;

import java.util.List;

public class GetChildIDResponse {
    public class Child {
        private String name;

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }
        private List<Child> children;

        // Getters and Setters
        public List<Child> getChildren() {
            return children;
        }

        public void setChildren(List<Child> children) {
            this.children = children;
        }

}
