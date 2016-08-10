package com.vaadin.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.data.Binder.Binding;
import com.vaadin.server.AbstractErrorMessage;
import com.vaadin.server.ErrorMessage;
import com.vaadin.server.UserError;
import com.vaadin.tests.data.bean.Person;
import com.vaadin.ui.AbstractField;

public class BinderTest {

    class TextField extends AbstractField<String> {

        String value = "";

        @Override
        public String getValue() {
            return value;
        }

        @Override
        protected void doSetValue(String value) {
            this.value = value;
        }
    }

    Binder<Person> binder;

    TextField nameField;

    Person p = new Person();

    @Before
    public void setUp() {
        binder = new Binder<>();
        p.setFirstName("Johannes");
        nameField = new TextField();
    }

    @Test(expected = NullPointerException.class)
    public void bindingNullBeanThrows() {
        binder.bind(null);
    }

    @Test(expected = NullPointerException.class)
    public void bindingNullFieldThrows() {
        binder.forField(null);
    }

    @Test(expected = NullPointerException.class)
    public void bindingNullGetterThrows() {
        binder.bind(nameField, null, Person::setFirstName);
    }

    @Test
    public void fieldValueUpdatedOnBeanBind() {
        binder.forField(nameField).bind(Person::getFirstName,
                Person::setFirstName);
        binder.bind(p);
        assertEquals("Johannes", nameField.getValue());
    }

    @Test
    public void fieldValueUpdatedWithShortcutBind() {
        bindName();
        assertEquals("Johannes", nameField.getValue());
    }

    @Test
    public void fieldValueUpdatedIfBeanAlreadyBound() {
        binder.bind(p);
        binder.bind(nameField, Person::getFirstName, Person::setFirstName);

        assertEquals("Johannes", nameField.getValue());
        nameField.setValue("Artur");
        assertEquals("Artur", p.getFirstName());
    }

    @Test
    public void getBeanReturnsBoundBeanOrNothing() {
        assertFalse(binder.getBean().isPresent());
        binder.bind(p);
        assertSame(p, binder.getBean().get());
        binder.unbind();
        assertFalse(binder.getBean().isPresent());
    }

    @Test
    public void fieldValueSavedToPropertyOnChange() {
        bindName();
        nameField.setValue("Henri");
        assertEquals("Henri", p.getFirstName());
    }

    @Test
    public void fieldValueNotSavedAfterUnbind() {
        bindName();
        nameField.setValue("Henri");
        binder.unbind();
        nameField.setValue("Aleksi");
        assertEquals("Henri", p.getFirstName());
    }

    @Test
    public void bindNullSetterIgnoresValueChange() {
        binder.bind(nameField, Person::getFirstName, null);
        binder.bind(p);
        nameField.setValue("Artur");
        assertEquals(p.getFirstName(), "Johannes");
    }

    @Test
    public void bindToAnotherBeanStopsUpdatingOriginalBean() {
        bindName();
        nameField.setValue("Leif");

        Person p2 = new Person();
        p2.setFirstName("Marlon");
        binder.bind(p2);
        assertEquals("Marlon", nameField.getValue());
        assertEquals("Leif", p.getFirstName());
        assertSame(p2, binder.getBean().get());

        nameField.setValue("Ilia");
        assertEquals("Ilia", p2.getFirstName());
        assertEquals("Leif", p.getFirstName());
    }

    @Test
    public void save_unbound_noChanges() {
        Binder<Person> binder = new Binder<>();
        Person person = new Person();

        int age = 10;
        person.setAge(age);

        binder.save(person);

        Assert.assertEquals(age, person.getAge());
    }

    @Test
    public void save_bound_beanIsUpdated() {
        Binder<Person> binder = new Binder<>();
        binder.bind(nameField, Person::getFirstName, Person::setFirstName);

        Person person = new Person();

        String fieldValue = "bar";
        nameField.setValue(fieldValue);

        person.setFirstName("foo");

        binder.save(person);

        Assert.assertEquals(fieldValue, person.getFirstName());
    }

    @Test
    public void load_bound_fieldValueIsUpdated() {
        Binder<Person> binder = new Binder<>();
        binder.bind(nameField, Person::getFirstName, Person::setFirstName);

        Person person = new Person();

        String name = "bar";
        person.setFirstName(name);
        binder.load(person);

        Assert.assertEquals(name, nameField.getValue());
    }

    @Test
    public void load_unbound_noChanges() {
        Binder<Person> binder = new Binder<>();

        nameField.setValue("");

        Person person = new Person();

        String name = "bar";
        person.setFirstName(name);
        binder.load(person);

        Assert.assertEquals("", nameField.getValue());
    }

    @Test
    public void validate_notBound_noErrors() {
        Binder<Person> binder = new Binder<>();

        List<ValidationError<?>> errors = binder.validate();

        Assert.assertTrue(errors.isEmpty());
    }

    @Test
    public void bound_validatorsAreOK_noErrors() {
        Binder<Person> binder = new Binder<>();
        Binding<Person, String, String> binding = binder.forField(nameField);
        binding.withValidator(Validator.alwaysPass()).bind(Person::getFirstName,
                Person::setFirstName);

        nameField.setComponentError(new UserError(""));
        List<ValidationError<?>> errors = binder.validate();

        Assert.assertTrue(errors.isEmpty());
        Assert.assertNull(nameField.getComponentError());
    }

    @SuppressWarnings("serial")
    @Test
    public void bound_validatorsFail_errors() {
        Binder<Person> binder = new Binder<>();
        Binding<Person, String, String> binding = binder.forField(nameField);
        binding.withValidator(Validator.alwaysPass());
        String msg1 = "foo";
        String msg2 = "bar";
        binding.withValidator(new Validator<String>() {
            @Override
            public Result<String> apply(String value) {
                return new SimpleResult<>(null, msg1);
            }
        });
        binding.withValidator(value -> false, msg2);
        binding.bind(Person::getFirstName, Person::setFirstName);

        List<ValidationError<?>> errors = binder.validate();

        Assert.assertEquals(2, errors.size());

        Set<String> errorMessages = errors.stream()
                .map(ValidationError::getMessage).collect(Collectors.toSet());
        Assert.assertTrue(errorMessages.contains(msg1));
        Assert.assertTrue(errorMessages.contains(msg2));

        Set<?> fields = errors.stream().map(ValidationError::getField)
                .collect(Collectors.toSet());
        Assert.assertEquals(1, fields.size());
        Assert.assertTrue(fields.contains(nameField));

        ErrorMessage componentError = nameField.getComponentError();
        Assert.assertNotNull(componentError);
        Assert.assertEquals("foo",
                ((AbstractErrorMessage) componentError).getMessage());
    }

    private void bindName() {
        binder.bind(nameField, Person::getFirstName, Person::setFirstName);
        binder.bind(p);
    }

}
