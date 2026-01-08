import { useRef, useState, useEffect } from 'react';

function CodeInput({ length = 6, value, onChange, disabled = false }) {
  const [values, setValues] = useState(Array(length).fill(''));
  const inputRefs = useRef([]);

  // Sync with external value
  useEffect(() => {
    if (value !== undefined) {
      const newValues = value.split('').slice(0, length);
      while (newValues.length < length) {
        newValues.push('');
      }
      setValues(newValues);
    }
  }, [value, length]);

  const focusInput = (index) => {
    if (inputRefs.current[index]) {
      inputRefs.current[index].focus();
    }
  };

  const handleChange = (index, e) => {
    const newValue = e.target.value;
    
    // Handle paste
    if (newValue.length > 1) {
      const pastedValue = newValue.slice(0, length - index);
      const newValues = [...values];
      
      for (let i = 0; i < pastedValue.length; i++) {
        if (index + i < length) {
          newValues[index + i] = pastedValue[i].toUpperCase();
        }
      }
      
      setValues(newValues);
      onChange(newValues.join(''));
      
      // Focus on next empty or last input
      const nextIndex = Math.min(index + pastedValue.length, length - 1);
      focusInput(nextIndex);
      return;
    }

    // Handle single character
    const char = newValue.slice(-1).toUpperCase();
    const newValues = [...values];
    newValues[index] = char;
    setValues(newValues);
    onChange(newValues.join(''));

    // Auto-focus next input
    if (char && index < length - 1) {
      focusInput(index + 1);
    }
  };

  const handleKeyDown = (index, e) => {
    if (e.key === 'Backspace') {
      if (!values[index] && index > 0) {
        // Move to previous input if current is empty
        focusInput(index - 1);
        const newValues = [...values];
        newValues[index - 1] = '';
        setValues(newValues);
        onChange(newValues.join(''));
      } else {
        // Clear current input
        const newValues = [...values];
        newValues[index] = '';
        setValues(newValues);
        onChange(newValues.join(''));
      }
    } else if (e.key === 'ArrowLeft' && index > 0) {
      focusInput(index - 1);
    } else if (e.key === 'ArrowRight' && index < length - 1) {
      focusInput(index + 1);
    }
  };

  const handlePaste = (e) => {
    e.preventDefault();
    const pastedData = e.clipboardData.getData('text').slice(0, length).toUpperCase();
    const newValues = pastedData.split('');
    while (newValues.length < length) {
      newValues.push('');
    }
    setValues(newValues);
    onChange(newValues.join(''));
    
    // Focus on last filled or last input
    const lastFilledIndex = Math.min(pastedData.length, length) - 1;
    focusInput(lastFilledIndex >= 0 ? lastFilledIndex : 0);
  };

  const handleFocus = (e) => {
    e.target.select();
  };

  return (
    <div className="flex justify-center gap-2 sm:gap-3">
      {values.map((val, index) => (
        <input
          key={index}
          ref={(el) => (inputRefs.current[index] = el)}
          type="text"
          inputMode="text"
          maxLength={length}
          value={val}
          onChange={(e) => handleChange(index, e)}
          onKeyDown={(e) => handleKeyDown(index, e)}
          onPaste={handlePaste}
          onFocus={handleFocus}
          disabled={disabled}
          className={`
            w-11 h-14 sm:w-14 sm:h-16 
            text-center text-xl sm:text-2xl font-bold
            border-2 rounded-lg
            focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500
            transition duration-200
            ${disabled 
              ? 'bg-gray-100 border-gray-200 text-gray-400 cursor-not-allowed' 
              : val 
                ? 'border-blue-500 bg-blue-50 text-blue-700' 
                : 'border-gray-300 bg-white text-gray-900 hover:border-gray-400'
            }
          `}
          autoComplete="one-time-code"
        />
      ))}
    </div>
  );
}

export default CodeInput;