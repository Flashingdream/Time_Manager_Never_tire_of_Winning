<template>
    <div class="container content-field">
        <div class="card">
            <div class="card-body">
                <slot></slot>
            </div>
        </div>
    </div>
</template>

<script>

</script>

<style scoped>
div.content-field {
    margin-top: 20px;
}

.card {
    border-radius: 16px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    transition: box-shadow 0.3s ease;
    background: rgba(255, 255, 255, 0.72);
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
    overflow: hidden;
}

.card:hover {
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.10);
}
</style>
