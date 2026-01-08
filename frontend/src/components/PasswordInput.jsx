import { useState, useMemo } from 'react';

function PasswordInput({ 
  value, 
  onChange, 
  name = 'password', 
  placeholder = 'Enter password',
  showStrength = false,
  showRequirements = false,
  label = 'Password',
  required = false,
  autoComplete = 'new-password'
}) {
  const [showPassword, setShowPassword] = useState(false);
  const [focused, setFocused] = useState(false);

  const requirements = useMemo(() => {
    return {
      minLength: value.length >= 8,
      hasUppercase: /[A-Z]/.test(value),
      hasLowercase: /[a-z]/.test(value),
      hasNumber: /[0-9]/.test(value),
      hasSpecial: /[!@#$%^&*(),.?":{}|<>]/.test(value),
    };
  }, [value]);

  const strength = useMemo(() => {
    const metRequirements = Object.values(requirements).filter(Boolean).length;
    if (metRequirements === 0) return { level: 0, label: '', color: 'bg-gray-200' };
    if (metRequirements <= 2) return { level: 1, label: 'Weak', color: 'bg-red-500' };
    if (metRequirements <= 3) return { level: 2, label: 'Fair', color: 'bg-yellow-500' };
    if (metRequirements <= 4) return { level: 3, label: 'Good', color: 'bg-blue-500' };
    return { level: 4, label: 'Strong', color: 'bg-green-500' };
  }, [requirements]);

  const requirementsList = [
    { key: 'minLength', label: 'At least 8 characters', met: requirements.minLength },
    { key: 'hasUppercase', label: 'One uppercase letter', met: requirements.hasUppercase },
    { key: 'hasLowercase', label: 'One lowercase letter', met: requirements.hasLowercase },
    { key: 'hasNumber', label: 'One number', met: requirements.hasNumber },
    { key: 'hasSpecial', label: 'One special character (!@#$%^&*)', met: requirements.hasSpecial },
  ];

  return (
    <div className="w-full">
      {label && (
        <label htmlFor={name} className="block text-sm font-medium text-gray-700 mb-1">
          {label} {required && <span className="text-red-500">*</span>}
        </label>
      )}
      
      <div className="relative">
        <input
          type={showPassword ? 'text' : 'password'}
          id={name}
          name={name}
          value={value}
          onChange={onChange}
          onFocus={() => setFocused(true)}
          onBlur={() => setFocused(false)}
          required={required}
          autoComplete={autoComplete}
          className="w-full px-4 py-3 pr-12 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition duration-200"
          placeholder={placeholder}
        />
        <button
          type="button"
          onClick={() => setShowPassword(!showPassword)}
          className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-700 focus:outline-none"
          tabIndex={-1}
        >
          {showPassword ? (
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M3.707 2.293a1 1 0 00-1.414 1.414l14 14a1 1 0 001.414-1.414l-1.473-1.473A10.014 10.014 0 0019.542 10C18.268 5.943 14.478 3 10 3a9.958 9.958 0 00-4.512 1.074l-1.78-1.781zm4.261 4.26l1.514 1.515a2.003 2.003 0 012.45 2.45l1.514 1.514a4 4 0 00-5.478-5.478z" clipRule="evenodd" />
              <path d="M12.454 16.697L9.75 13.992a4 4 0 01-3.742-3.741L2.335 6.578A9.98 9.98 0 00.458 10c1.274 4.057 5.065 7 9.542 7 .847 0 1.669-.105 2.454-.303z" />
            </svg>
          ) : (
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
              <path d="M10 12a2 2 0 100-4 2 2 0 000 4z" />
              <path fillRule="evenodd" d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clipRule="evenodd" />
            </svg>
          )}
        </button>
      </div>

      {/* Strength Indicator */}
      {showStrength && value.length > 0 && (
        <div className="mt-2">
          <div className="flex items-center gap-2">
            <div className="flex-1 h-1.5 bg-gray-200 rounded-full overflow-hidden flex gap-0.5">
              {[1, 2, 3, 4].map((level) => (
                <div
                  key={level}
                  className={`flex-1 h-full transition-colors duration-300 ${
                    level <= strength.level ? strength.color : 'bg-gray-200'
                  }`}
                />
              ))}
            </div>
            <span className={`text-xs font-medium ${
              strength.level <= 1 ? 'text-red-600' :
              strength.level === 2 ? 'text-yellow-600' :
              strength.level === 3 ? 'text-blue-600' :
              'text-green-600'
            }`}>
              {strength.label}
            </span>
          </div>
        </div>
      )}

      {/* Requirements List */}
      {showRequirements && (focused || value.length > 0) && (
        <div className="mt-3 p-3 bg-gray-50 rounded-lg border border-gray-200">
          <p className="text-xs font-medium text-gray-600 mb-2">Password must contain:</p>
          <ul className="space-y-1">
            {requirementsList.map((req) => (
              <li key={req.key} className="flex items-center gap-2 text-xs">
                {req.met ? (
                  <svg className="h-4 w-4 text-green-500" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                  </svg>
                ) : (
                  <svg className="h-4 w-4 text-gray-400" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-11a1 1 0 10-2 0v2H7a1 1 0 100 2h2v2a1 1 0 102 0v-2h2a1 1 0 100-2h-2V7z" clipRule="evenodd" />
                  </svg>
                )}
                <span className={req.met ? 'text-green-700' : 'text-gray-500'}>
                  {req.label}
                </span>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}

export default PasswordInput;