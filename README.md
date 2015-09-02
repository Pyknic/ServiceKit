# ServiceKit
A lightweight java-library that use annotations to create web-services from ordinary java methods.

## Usage
To create a new server using ServiceKit, create a new class and extend `HttpServer`. Once you execute the inherited `start()`-method, every request to annoted `@Service`-methods will be handled automatically.

```java
class ExampleServer extends HttpServer {
    public ExampleServer() {
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

### Straight-forward error handling
```java
final Item[] items;
...
@Service({"index"})
public Item findItem(int index) {
    if (index < 0 || index >= items.length) {
        throw new HttpResponseException(
            Status.BAD_REQUEST, 
            "Requested item index is out of bounds."
        );
    }
        
    return items[index];
}
```

```
http://example.com:1234/findItem?index=-47
-> 400 BAD REQUEST: Requested item index is out of bounds.
```

### (Optional) Fast caching
If a cache is specified in the annotation it can reduce load on the server significally. The annoted method will only be called once for each set of input. The BasicCache-class is setup so that values will be overwritten each hour.

```java
@Service({"year"}, cache = BasicCache.class)
public Item totalUsageTime(int year) { 
    return usage.stream()
        .filter(u -> u.getYear() == year)
        .mapToInt(u -> u.getTime())
        .sum();
}
```
