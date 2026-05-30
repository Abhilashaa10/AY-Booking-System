```java id="l6hy2r"
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLog {

    private String id;
    private String bookingId;
    private String email;
    private String status;

}
```
