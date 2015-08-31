# ServiceKit
A lightweight java-library that use annotations to create web-services from ordinary java methods.

## Usage
To create a new server using ServiceKit, create a new class and extend `HttpServlet`. Once you execute the inherited `start()`-method, every request to annoted `@Service`-methods will be handled automatically.

```java
class ExampleServer extends HttpServer {
    public ExampleServlet() {
        super (1234);
        start();
    }
    ...
    // @Service methods here...
}
```

## Examples
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

### Objects are parsed to/from json by default
```java
class Person {
    private final String firstname, lastname;
    
    public Person(String firstname, String lastname) {
        this.firstname = firstname;
        this.lastname  = lastname;
    }
    
    public String getFirstname() {
        return firstname;
    }
    
    public String getLastname() {
        return lastname;
    }
}
...
@Service({"you"})
public String hi(Person you) {
    return "Hello, " + you.getFirstname() + " " + you.getLastname() + "!";
}
```

```
http://example.com:1234/hi?you={"firstname":"John","lastname":"Smith"}
-> "Hello, John Smith!"
```

### Complex return types are automatically parsed into JSON
```java
class Car {
    private final String name;
    private final int cost;
    
    public Car(String name, int cost) {
        this.name = name;
        this.cost = cost;
    }
}
...
@Service()
public Car favoriteCar() {
    return new Car("Tesla", 80_000); // USD
}
```

```
http://example.com:1234/favoriteCar
-> { name : "Tesla", cost : 80000 }
```

### You can use a custom encoder to format output
In JSONP, the output should be passed to javascript function of a particular name. [Learn more here!](https://en.wikipedia.org/wiki/JSONP)

```java
class Car {
    private final String name;
    private final int cost;
    
    public Car(String name, int cost) {
        this.name = name;
        this.cost = cost;
    }
}
...
@Service(encoder = JsonpEncoder.class, params = {"callback"})
public Car favoriteCar(String callback) {
    return new Car("Tesla", 80_000); // USD
}
```

```
http://example.com:1234/favoriteCar?callback=myCallbackMethod
-> myCallbackMethod({ name : "Tesla", cost : 80000});
```
