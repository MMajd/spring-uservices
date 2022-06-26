package mmajd.util.http;

import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
public class HttpErrorInfo {
  private final HttpStatus httpStatus;
  private final String message;
  private final String path;
  private final ZonedDateTime timestamp;
}
