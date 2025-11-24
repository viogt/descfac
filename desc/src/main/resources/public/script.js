async function uploadFile() {
  const fileInput = document.getElementById("fileInput");
  const file = fileInput.files[0]; // Get the first selected file
  const messageElement = document.getElementById("message");

  /*if (!file || !file.name.endsWith(".xlsx")) {
    messageElement.innerHTML = "<font color='red'>Please select an .XLSX file.</font>";
    return;
  }*/
  messageElement.innerHTML = "<span class='loader'></span> Processing...";

  const formData = new FormData();
  formData.append("myFile", file);

  try {
    const response = await fetch("/upload", {
      method: "POST",
      body: formData,
    });

    if (response.ok) messageElement.innerHTML = await response.text();
    else {
      const errorText = await response.text();
      messageElement.textContent = `Upload failed: ${response.status} - ${errorText}`;
    }
  } catch (error) {
    messageElement.textContent = `Network error: ${error.message}`;
    console.error("Fetch error:", error);
  }
}
