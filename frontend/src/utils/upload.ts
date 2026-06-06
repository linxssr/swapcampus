export function isImageFile(file: File) {
  return file.type.startsWith('image/');
}

export function toFormData(file: File, fieldName = 'file') {
  const formData = new FormData();
  formData.append(fieldName, file);
  return formData;
}
