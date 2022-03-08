package mockito.learning;

import com.mockito.learning.PersonCtlr;
import com.mockito.learning.PersonService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PersonCtlrTest {


    @Mock
    PersonService personService;

    @Captor
    ArgumentCaptor<String> setRoleCaptor;

//    @Before
//    public void init() {
//        MockitoAnnotations.initMocks(this);
//    }

    @Test
    public void testPersonCtlrDetails() {

        // PersonService personService = mock(PersonService.class);
        when(personService.getName()).thenReturn("def");
        when(personService.getAge()).thenReturn("456");
        PersonCtlr personCtlr = new PersonCtlr(personService);
        Assert.assertEquals("detail did not match", "def : 456", personCtlr.getDetails());
        verify(personService, times(1)).getAge();
        verify(personService, times(2)).setRole(setRoleCaptor.capture());
        List<String> allValues = setRoleCaptor.getAllValues();
        Assert.assertEquals(Arrays.asList("Role123", "Role789"), allValues);
    }
}
