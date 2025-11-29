export interface Instructor {
    firstName: string;
    lastName: string;
    phone: string;
    email: string;
    gender?: string; 
    dateOfBirth?: string; 
    profilePhoto?: string; 
    city: string;
    status?: string; // Optionnel
    bio?: string; // Optionnel
    speciality?: string; // Optionnel
    password: string; 
    confirmPassword: string
  }
  