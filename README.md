# ServiceKit
A lightweight java-library that use annotations to create web-services from ordinary java methods.

# Examples
### Simple return type
```java
@Service
public String hi() {
    return "Hello, world!";
}
```

```
http://example.com:1234/hi 
-> "Hello, world!"
```

### Provide parameters
```java
@Service({"name"})
public String hi(String name) {
    return "Hello, " + name + "!";
}
```

```
http://example.com:1234/hi?name=John
-> "Hello, John!"
```

### Objects are parsed to/from json
```java
class Person {
    private final String firstname, lastname;
    
    public Person(String firstname, String lastname) {
        this.firstname = firstname;
        this.lastname = lastname;
    }
    
    public String getFirstname() {
        return firstname;
    }
    
    public String getLastname() {
        return lastname;
    }
}

@Service({"you"})
public String hi(Person you) {
    return "Hello, " + you.getFirstname() + " " + you.getLastname() + "!";
}
```

```
http://example.com:1234/hi?you={"firstname":"John","lastname":"Smith"}
-> "Hello, John Smith!"
```
